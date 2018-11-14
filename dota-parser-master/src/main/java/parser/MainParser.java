package parser;

import org.apache.commons.compress.compressors.CompressorException;
import parser.mouse.MouseParser;
import parser.stats.StatParser;
import skadistats.clarity.event.Insert;
import skadistats.clarity.model.Entity;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.reader.OnMessage;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.resources.Resources;
import skadistats.clarity.processor.resources.UsesResources;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.ControllableRunner;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.wire.common.proto.DotaUserMessages;
import skadistats.clarity.wire.s2.proto.S2DotaGcCommon;
import util.ClarityUtil;
import util.DotaReplayStream;

import java.io.File;
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
    private StatParser statParser;

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
    }

    public MainParser(String replayFile, long filterSteamID) {
        this(replayFile);
        this.filterSteamID = filterSteamID;
    }

    public MainParser(String filepath, long filterSteamID, boolean isDir) {
        this("", filterSteamID);
        this.replayDir = new File(filepath);
    }

    @OnMessage(S2DotaGcCommon.CMsgDOTAMatch.class)
    public void matchMessage(S2DotaGcCommon.CMsgDOTAMatch message) {
        System.out.println("Pre game: " + message.getPreGameDuration());
        System.out.println("Duration: " + message.getDuration());

        statParser.duration = (float) Math.ceil(message.getDuration()/60.0);

        PlayerData pd = steamPDMap.get(filterSteamID);
        statParser.writeStats(ctx, pd.getPlayerID(), pd.getTeamName(), outputName + "-playerstats.csv");
    }

    public void start() {
        if (replayDir != null) {
            for (File replayFile : replayDir.listFiles()) {
                try {
                    this.replayFile = replayDir.getAbsolutePath() + "/" + replayFile.getName();
                    this.filterSteamID = Long.parseLong(replayFile.getName().substring(0, 17));

                    initParsers();

                    String outputName = replayDir.getAbsolutePath() + "/../data/" + replayFile.getName();
                    mouseParser.initWriter(outputName + "-mousesequence.csv", outputName + "-mouseaction.csv");

                    initProcessing();
                    run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (mouseParser != null) {
                        mouseParser.closeWriter();
                    }
                }
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
        this.statParser = new StatParser(filterSteamID);
    }

    /**
     * Fill in data structures at the start of a match using a ControllableRunner
     */
    private void initProcessing() {
        ControllableRunner cRunner = null;
        try {
            steamPDMap = new HashMap<>();
            idSteamMap = new HashMap<>();
            idMap = new HashMap<>();

            cRunner = new ControllableRunner(new DotaReplayStream(replayFile, true)).runWith(this);
            for (int i = 0 ; i < 30000; i++) {
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
                    int teamNum = ClarityUtil.getEntityProperty(playerEntity, "m_iTeamNum");

                    PlayerData pd = new PlayerData(playerID, heroID, selectedHero, teamNum, steamID);
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
            SimpleRunner sRunner = new SimpleRunner(new DotaReplayStream(replayFile, true)).runWith(this);

        } catch (IOException | CompressorException e) {
            e.printStackTrace();
        } finally {
            running = false;
        }
        running = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (mouseParser != null) {
            mouseParser.tick();
        }

        if (statParser != null) {
            statParser.tick();
        }
    }

    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        if (running && idMap != null) {
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


    @OnMessage(DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders.class)
    public void onSpectatorPlayerUnitOrders(Context ctx, DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
        if (running) {
            Entity e = ctx.getProcessor(Entities.class).getByIndex(msg.getEntindex());
            if ((filterSteamID != NO_FILTER && isFilterPlayer(e)) || (filterSteamID == NO_FILTER && ClarityUtil.isGamePlayer(e))) {
                long steamid = idSteamMap.get(ClarityUtil.getEntityProperty(e, "m_iPlayerID"));
                mouseParser.parseUnitOrder(steamid, msg);
                statParser.parseUnitOrder(steamid, msg);
            }
        }
    }


    public boolean isFilterPlayer(Entity e) {
        int playerID = steamPDMap.get(filterSteamID).getPlayerID();
        int entityID = ClarityUtil.getEntityProperty(e, "m_iPlayerID");

        return (ClarityUtil.isPlayer(e) && entityID == playerID);
    }

}
