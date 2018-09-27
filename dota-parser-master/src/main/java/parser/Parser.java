package parser;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import resolver.impl.DefaultResolver;
import skadistats.clarity.event.Insert;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.model.GameEvent;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.OnEntityDeleted;
import skadistats.clarity.processor.entities.OnEntityUpdated;
import skadistats.clarity.processor.gameevents.OnGameEvent;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.ControllableRunner;
import skadistats.clarity.processor.runner.OnInit;
import skadistats.clarity.processor.runner.SimpleRunner;
import util.DotaReplayStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class Parser {

	public static final int RADIANT = 2;
	public static final int DIRE = 3;
	
    private final Logger log = LoggerFactory.getLogger(Main.class.getPackage().getClass());
    
    @Insert
    private Context ctx;

    private HashMap<Long, PlayerData> steamIDMap;

    private String replayFile;


    public Parser(String replayFile) throws Exception {
        this.replayFile = replayFile;
        this.steamIDMap = new HashMap<>();

        initProcessing();
    }

    /**
     * Fill in data structures at the start of a match using a ControllableRunner
     */
    private void initProcessing() {
        ControllableRunner cRunner = null;
        try {
            cRunner = new ControllableRunner(new DotaReplayStream(replayFile, true)).runWith(this);

            Entity playerResource;
            do {
                // Tick runner until the PlayerResource Entity is spawned
                cRunner.tick();
            }
            while ((playerResource = getEntity(ctx, "CDOTA_PlayerResource")) == null);

            int numPlayers = getEntityProperty(playerResource, "m_vecPlayerData");
            System.out.println(playerResource);
            System.out.println(numPlayers);

            for (int i = 0; i < numPlayers; i++) {
                DefaultResolver<Integer> steamIDResolver = new DefaultResolver<>(ctx, "CDOTA_PlayerResource", "m_vecPlayerData.%i.m_iPlayerSteamID");
                DefaultResolver<String> playerNameResolver = new DefaultResolver<>(ctx, "CDOTA_PlayerResource", "m_vecPlayerData.%i.m_iszPlayerName");
                System.out.println(steamIDResolver.resolveValue(i, 0, 0) + " - " + playerNameResolver.resolveValue(i, 0, 0));
            }

        } catch (IOException | CompressorException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (cRunner != null) {
                cRunner.halt();
            }
        }
    }

    public static Entity getEntity(Context ctx, String entityName) {
        if (ctx != null) {
            return ctx.getProcessor(Entities.class).getByDtName(entityName);
        }
        return null;
    }

    public void run(String[] args) throws Exception {
//        cRunner.tick();
//
//        cRunner.getContext().getProcessor(Entities.class).getByDtName("CDOTA_PlayerResource");
//        long tStart = System.currentTimeMillis();
//        SimpleRunner r = null;
//        try {
//            r = new SimpleRunner(new DotaReplayStream("replay.dem.bz2", true)).runWith(this);
//        } finally {
//            long tMatch = System.currentTimeMillis() - tStart;
//            log.info("total time taken: {}s", (tMatch) / 1000.0);
//            if (r != null) {
//                r.getSource().close();
//            }
//        }
    }
    private boolean isPlayer(Entity e) {
    	return e.getDtClass().getDtName().startsWith("CDOTAPlayer");
    }
   
    private boolean isPlayer(Entity e, int id) {
    	if (isPlayer(e)) {
    		int playerID = getEntityProperty(e, "m_iPlayerID");
    		return playerID == id;
    	}
    	return false;
    }
    
    private boolean isTeamPlayer(Entity e, int teamNum) {
    	if (isPlayer(e)) {
    		int playerTeamNum = getEntityProperty(e, "m_iTeamNum");
    		return playerTeamNum == teamNum;
    	}
    	return false;
    }

    
    @OnTickStart
    public void onTickstart(Context ctx, boolean syntheic) {
        System.out.println("init");
        ctx.getProcessor(Entities.class).getByDtName("CDOTA_PlayerResource");

    }


    @OnEntityUpdated
    public void onUpdated(Entity e, FieldPath[] updatedPaths, int updateCount) {
    	int targetPlayerID = 1;
    	if (isPlayer(e, targetPlayerID)) {
    		int playerID = getEntityProperty(e, "m_iPlayerID");
    		int mouseX = getEntityProperty(e, "m_iCursor.0000");
    		int mouseY = getEntityProperty(e, "m_iCursor.0001");
    		System.out.println(String.format("player %d - mouse x: %d, mouse y: %d", playerID, mouseX, mouseY));
    	}
    }
    
    private <T> T getEntityProperty(Entity e, String property) {
    	try {
            FieldPath f = e.getDtClass().getFieldPathForName(property);
            return e.getPropertyForFieldPath(f);
        } catch (Exception x) {
            return null;
        }
    }



}
