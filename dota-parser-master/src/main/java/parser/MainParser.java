package parser;

import org.apache.commons.compress.compressors.CompressorException;
import parser.mouse.MouseParser;
import skadistats.clarity.event.Insert;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.OnEntityUpdated;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.reader.OnMessage;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.resources.Resources;
import skadistats.clarity.processor.resources.UsesResources;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.ControllableRunner;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.wire.common.proto.DotaUserMessages;
import util.ClarityUtil;
import util.DotaReplayStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@UsesResources
@UsesEntities
public class MainParser extends Parser{

	public static final int RADIANT = 2;
	public static final int DIRE = 3;

	public static final long NO_FILTER = -1;

	public static final int TICK_RATE = 30;
	
    @Insert
    private Context ctx;

    @Insert
    private Resources resources;

    private String replayFile;
    private long filterSteamID;

    private boolean running;

    private File replayDir;
    private MouseParser mouseParser;

    private HashMap<Long, PlayerData> steamPDMap;
    private HashMap<Integer, Long> idSteamMap;
    private HashMap<Integer, Entity> idMap;


    public MainParser(String replayFile) {
        super(NO_FILTER);
        this.replayFile = replayFile;
        this.filterSteamID = NO_FILTER;

        this.running = false;

        this.steamPDMap = new HashMap<>();
        this.idSteamMap = new HashMap<>();
        this.idMap = new HashMap<>();

        initParsers();
    }

    public MainParser(String replayFile, long filterSteamID) {
        this(replayFile);
        this.filterSteamID = filterSteamID;

        initParsers();
    }

    public MainParser(String filepath, long filterSteamID, boolean isDir) {
        this("", filterSteamID);
        this.replayDir = new File(filepath);
    }

    public void start() {
        if (replayDir != null) {
            try {
                for (File replayFile : replayDir.listFiles()) {
                    this.replayFile = replayDir.getAbsolutePath() + "/" + replayFile.getName();

                    String outputName = filterSteamID + "-" + replayFile.getName();
                    mouseParser.initWriter(outputName + "-mousesequence.csv", outputName + "-mouseaction.csv");
                    initProcessing();
                    run();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                mouseParser.closeWriter();
            }
        }
        else {
            initProcessing();
            run();
        }

        System.out.println("Finish " + filterSteamID);
    }

    private void initParsers() {
        this.mouseParser = new MouseParser(filterSteamID);
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

            Iterator<Entity> playerEntities = ClarityUtil.getEntities(ctx, "CDOTAPlayer");
            while(playerEntities.hasNext()) {
                Entity playerEntity = playerEntities.next();
                if (ClarityUtil.isGamePlayer(playerEntity)) {
                    int playerID = ClarityUtil.getEntityProperty(playerEntity, "m_iPlayerID");
                    int heroID = ClarityUtil.resolveValue(ctx, "CDOTA_PlayerResource", "m_vecPlayerTeamData.%i.m_nSelectedHeroID", playerID, 0, 0);
                    int selectedHero = ClarityUtil.resolveValue(ctx, "CDOTA_PlayerResource", "m_vecPlayerTeamData.%i.m_hSelectedHero", playerID, 0, 0);
                    long steamID =  ClarityUtil.resolveValue(ctx, "CDOTA_PlayerResource", "m_vecPlayerData.%i.m_iPlayerSteamID", playerID, 0, 0);

                    PlayerData pd = new PlayerData(playerID, heroID, selectedHero, steamID);
                    steamPDMap.put(steamID, pd);
                    idSteamMap.put(playerID, steamID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cRunner != null) {
                cRunner.halt();
            }
        }
        System.out.println(filterSteamID + " - " + replayFile);
    }

    public void run() {
        gameTick = 0;
        running = true;
        try {
//            mouseParser.initWriter("mouseSequence.csv", "mouseAction.csv");

            SimpleRunner sRunner = new SimpleRunner(new DotaReplayStream(replayFile, true)).runWith(this);

        } catch (IOException | CompressorException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tick() {
        super.tick();
        mouseParser.tick();
    }

    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        if (running) {
            if (filterSteamID == NO_FILTER) {
                for (Map.Entry<Long, PlayerData> entry : steamPDMap.entrySet()) {
                    PlayerData pd = entry.getValue();
                    mouseParser.writeMouseMovements(pd, idMap.get(pd.getPlayerID()));
                }
            } else {
                PlayerData pd = steamPDMap.get(filterSteamID);
                mouseParser.writeMouseMovements(pd, idMap.get(pd.getPlayerID()));
            }
            tick();
        }

    }

    @OnEntityCreated
    public void OnEntityCreated(Entity e) {
        if (running) {
            if (ClarityUtil.isGamePlayer(e)) {
                int id = ClarityUtil.getEntityProperty(e, "m_iPlayerID");
                idMap.put(id, e);
            }
        }
    }

    @OnEntityUpdated
    public void onEntityUpdated(Entity e, FieldPath[] updatedPaths, int updateCount) {

    }


    @OnMessage(DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders.class)
    public void onSpectatorPlayerUnitOrders(Context ctx, DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
        if (running) {
            Entity e = ctx.getProcessor(Entities.class).getByIndex(msg.getEntindex());
            if ((filterSteamID != NO_FILTER && isFilterPlayer(e)) || (filterSteamID == NO_FILTER && ClarityUtil.isGamePlayer(e))) {
                long steamid = idSteamMap.get(ClarityUtil.getEntityProperty(e, "m_iPlayerID"));
                mouseParser.parseUnitOrder(steamid, msg);
            }
        }
    }


    public boolean isFilterPlayer(Entity e) {
        int playerID = steamPDMap.get(filterSteamID).getPlayerID();
        int entityID = ClarityUtil.getEntityProperty(e, "m_iPlayerID");

        return (ClarityUtil.isPlayer(e) && entityID == playerID);
    }

}
