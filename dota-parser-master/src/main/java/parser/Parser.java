package parser;

import org.apache.commons.compress.compressors.CompressorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import parser.PlayerData;
import resolver.impl.DefaultResolver;
import skadistats.clarity.decoder.Util;
import skadistats.clarity.event.Insert;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.OnEntityUpdated;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.ControllableRunner;
import skadistats.clarity.processor.runner.SimpleRunner;
import util.DotaReplayStream;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Parser {

	public static final int RADIANT = 2;
	public static final int DIRE = 3;

	public static final long NO_FILTER = -1;
	
//    private final Logger log = LoggerFactory.getLogger(Main.class.getPackage().getClass());
    
    @Insert
    private Context ctx;
    private String replayFile;
    private long filterSteamID;
    private long gameTick;

    private boolean running;

    private HashMap<Long, PlayerData> steamIDMap;
    private HashMap<Integer, Entity> idMap;

    private PrintWriter mouseMovementWriter;


    public Parser(String replayFile) {
        this.replayFile = replayFile;
        this.filterSteamID = NO_FILTER;
        this.gameTick = 0;

        this.running = false;

        this.steamIDMap = new HashMap<>();
        this.idMap = new HashMap<>();

        initProcessing();
        run();
    }

    public Parser(String replayFile, long filterSteamID) {
        this(replayFile);
        this.filterSteamID = filterSteamID;
    }

    /**
     * Fill in data structures at the start of a match using a ControllableRunner
     */
    private void initProcessing() {
        ControllableRunner cRunner = null;
        try {
            cRunner = new ControllableRunner(new DotaReplayStream(replayFile, true)).runWith(this);
            for (int i = 0; i < 20000; i++) {
                cRunner.tick();
            }

            Iterator<Entity> playerEntities = getEntities(ctx, "CDOTAPlayer");
            while(playerEntities.hasNext()) {
                Entity playerEntity = playerEntities.next();
                if (isGamePlayer(playerEntity)) {
                    int playerID = getEntityProperty(playerEntity, "m_iPlayerID");
                    int heroID = resolveValue(ctx, "CDOTA_PlayerResource", "m_vecPlayerTeamData.%i.m_nSelectedHeroID", playerID, 0, 0);
                    long steamID =  resolveValue(ctx, "CDOTA_PlayerResource", "m_vecPlayerData.%i.m_iPlayerSteamID", playerID, 0, 0);

                    PlayerData pd = new PlayerData(playerID, heroID, steamID);
                    steamIDMap.put(steamID, pd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cRunner != null) {
                cRunner.halt();
            }
        }
    }

    public void run() {
        gameTick = 0;
        running = true;
        try {
            mouseMovementWriter = new PrintWriter("output.csv");
            mouseMovementWriter.write("steamID,tick,mouseX,mouseY\n");
            mouseMovementWriter.flush();

            SimpleRunner sRunner = new SimpleRunner(new DotaReplayStream(replayFile, true)).runWith(this);

        } catch (IOException | CompressorException e) {
            e.printStackTrace();
        } finally {
            mouseMovementWriter.flush();
            mouseMovementWriter.close();
        }
    }

    @OnEntityCreated
    public void OnEntityCreated(Context ctx, Entity e) {
        if (isGamePlayer(e)) {
            int id = getEntityProperty(e, "m_iPlayerID");
            idMap.put(id, e);
        }
    }


    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        if (running) {
            if (filterSteamID == NO_FILTER) {
                for (Map.Entry<Long, PlayerData> entry : steamIDMap.entrySet()) {
                    writeMouseMovements(entry.getValue());
                }
            } else {
                writeMouseMovements(steamIDMap.get(filterSteamID));
            }
            gameTick++;
        }

    }

    private void writeMouseMovements(PlayerData pd) {
        Entity player = idMap.get(pd.getPlayerID());

        if (player != null && pd != null) {
            long steamID = pd.getSteamID();
            long tick = gameTick;
            int mouseX = getEntityProperty(player, "m_iCursor.0000");
            int mouseY = getEntityProperty(player, "m_iCursor.0001");

            mouseMovementWriter.write(steamID + "," + tick + "," + mouseX + "," + mouseY + "\n");
            mouseMovementWriter.flush();
        }
    }


    private boolean isPlayer(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTAPlayer");
    }
    
    private boolean isGamePlayer(Entity e) {
    	if (isPlayer(e)) {
    		int playerTeamNum = getEntityProperty(e, "m_iTeamNum");
    		return playerTeamNum == RADIANT || playerTeamNum == DIRE;
    	}
    	return false;
    }

    private static String getTeamName(int team) {
        switch(team) {
            case 2: return "Radiant";
            case 3: return "Dire";
            default: return "";
        }
    }

    public static <T> T resolveValue(Context ctx, String entityName, String pattern, int index, int team, int pos) {
        String fieldPathString = pattern
                .replaceAll("%i", Util.arrayIdxToString(index))
                .replaceAll("%t", Util.arrayIdxToString(team))
                .replaceAll("%p", Util.arrayIdxToString(pos));
        String compiledName = entityName.replaceAll("%n", getTeamName(team));
        Entity entity = Parser.getEntity(ctx, compiledName);
        FieldPath fieldPath = entity.getDtClass().getFieldPathForName(fieldPathString);

        return entity.getPropertyForFieldPath(fieldPath);
    }

    public static <T> T getEntityProperty(Entity e, String property) {
    	try {
            FieldPath f = e.getDtClass().getFieldPathForName(property);
            return e.getPropertyForFieldPath(f);
        } catch (Exception x) {
            return null;
        }
    }

    public static Entity getEntity(Context ctx, String entityName) {
        if (ctx != null) {
            return ctx.getProcessor(Entities.class).getByDtName(entityName);
        }
        return null;
    }

    public static Iterator<Entity> getEntities(Context ctx, String entityName) {
        if (ctx != null) {
            return ctx.getProcessor(Entities.class).getAllByDtName(entityName);
        }
        return null;
    }

}
