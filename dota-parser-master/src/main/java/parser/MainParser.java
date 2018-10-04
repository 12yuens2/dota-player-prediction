package parser;

import main.Main;
import org.apache.commons.compress.compressors.CompressorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.mouse.MouseParser;
import skadistats.clarity.event.Insert;
import skadistats.clarity.model.CombatLogEntry;
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
	
    private final Logger log = LoggerFactory.getLogger(Main.class.getPackage().getClass());

    @Insert
    private Context ctx;

    @Insert
    private Resources resources;

    private String replayFile;
    private long filterSteamID;
    private long gameTick;

    private boolean running;

    private MouseParser mouseParser;

    private HashMap<Long, PlayerData> steamIDMap;
    private HashMap<Integer, Entity> idMap;


    public MainParser(String replayFile) {
        super(NO_FILTER);
        this.replayFile = replayFile;

        this.running = false;

        this.steamIDMap = new HashMap<>();
        this.idMap = new HashMap<>();

        initParsers();
    }

    public MainParser(String replayFile, long filterSteamID) {
        this(replayFile);
        this.filterSteamID = filterSteamID;

        initParsers();
    }

    public void start() {
        initProcessing();
        run();
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
            mouseParser.initWriter("mouseSequence.csv");

            SimpleRunner sRunner = new SimpleRunner(new DotaReplayStream(replayFile, true)).runWith(this);

        } catch (IOException | CompressorException e) {
            e.printStackTrace();
        } finally {
            mouseParser.closeWriter();
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
                for (Map.Entry<Long, PlayerData> entry : steamIDMap.entrySet()) {
                    PlayerData pd = entry.getValue();
                    mouseParser.writeMouseMovements(pd, idMap.get(pd.getPlayerID()));
                }
            } else {
                PlayerData pd = steamIDMap.get(filterSteamID);
                mouseParser.writeMouseMovements(pd, idMap.get(pd.getPlayerID()));
            }
            tick();
        }

    }

    @OnEntityCreated
    public void OnEntityCreated(Entity e) {
        if (ClarityUtil.isGamePlayer(e)) {
            int id = ClarityUtil.getEntityProperty(e, "m_iPlayerID");
            idMap.put(id, e);
        }
    }

    @OnEntityUpdated
    public void onEntityUpdated(Entity e, FieldPath[] updatedPaths, int updateCount) {

    }


    @OnMessage(DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders.class)
    public void onSpectatorPlayerUnitOrders(Context ctx, DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
        Entity e = ctx.getProcessor(Entities.class).getByIndex(msg.getEntindex());
        if (isFilterPlayer(e)) {
            mouseParser.parseUnitOrder(msg);
        }
    }

//    @OnCombatLogEntry
//    public void onCombatLogEntry(CombatLogEntry cle) {
//        if (getAttackerNameCompiled(cle).contains(" ")) {
//            String time = cle.getTimestamp() + ": " + gameTick;
//        switch (cle.getType()) {
//            case DOTA_COMBATLOG_DAMAGE:
//                log.info("{} {} hits {}{} for {} damage{}",
//                        time,
//                        getAttackerNameCompiled(cle),
//                        getTargetNameCompiled(cle),
//                        cle.getInflictorName() != null ? String.format(" with %s", cle.getInflictorName()) : "",
//                        cle.getValue(),
//                        cle.getHealth() != 0 ? String.format(" (%s->%s)", cle.getHealth() + cle.getValue(), cle.getHealth()) : ""
//                );
//                break;
//            case DOTA_COMBATLOG_HEAL:
//                log.info("{} {}'s {} heals {} for {} health ({}->{})",
//                        time,
//                        getAttackerNameCompiled(cle),
//                        cle.getInflictorName(),
//                        getTargetNameCompiled(cle),
//                        cle.getValue(),
//                        cle.getHealth() - cle.getValue(),
//                        cle.getHealth()
//                );
//                break;
//            case DOTA_COMBATLOG_MODIFIER_ADD:
//                log.info("{} {} receives {} buff/debuff from {}",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getInflictorName(),
//                        getAttackerNameCompiled(cle)
//                );
//                break;
//            case DOTA_COMBATLOG_MODIFIER_REMOVE:
//                log.info("{} {} loses {} buff/debuff",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getInflictorName()
//                );
//                break;
//            case DOTA_COMBATLOG_DEATH:
//                log.info("{} {} is killed by {}",
//                        time,
//                        getTargetNameCompiled(cle),
//                        getAttackerNameCompiled(cle)
//                );
//                break;
//            case DOTA_COMBATLOG_ABILITY:
//                log.info("{} {} {} ability {} (lvl {}){}{}",
//                        time,
//                        getAttackerNameCompiled(cle),
//                        cle.isAbilityToggleOn() || cle.isAbilityToggleOff() ? "toggles" : "casts",
//                        cle.getInflictorName(),
//                        cle.getAbilityLevel(),
//                        cle.isAbilityToggleOn() ? " on" : cle.isAbilityToggleOff() ? " off" : "",
//                        cle.getTargetName() != null ? " on " + getTargetNameCompiled(cle) : ""
//                );
//                break;
//            case DOTA_COMBATLOG_ITEM:
//                log.info("{} {} uses {}",
//                        time,
//                        getAttackerNameCompiled(cle),
//                        cle.getInflictorName()
//                );
//                break;
//            case DOTA_COMBATLOG_GOLD:
//                log.info("{} {} {} {} gold",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getValue() < 0 ? "looses" : "receives",
//                        Math.abs(cle.getValue())
//                );
//                break;
//            case DOTA_COMBATLOG_GAME_STATE:
//                log.info("{} game state is now {}",
//                        time,
//                        cle.getValue()
//                );
//                break;
//            case DOTA_COMBATLOG_XP:
//                log.info("{} {} gains {} XP",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getValue()
//                );
//                break;
//            case DOTA_COMBATLOG_PURCHASE:
//                log.info("{} {} buys item {}",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getValueName()
//                );
//                break;
//            case DOTA_COMBATLOG_BUYBACK:
//                log.info("{} player in slot {} has bought back",
//                        time,
//                        cle.getValue()
//                );
//                break;
//
//            default:
////                DotaUserMessages.DOTA_COMBATLOG_TYPES type = cle.getType();
////                log.info("\n{} ({})\n", type.name(), type.ordinal());
//                break;
//        }
//        }
//    }

    private String compileName(String attackerName, boolean isIllusion) {
        return attackerName != null ? attackerName + (isIllusion ? " (illusion)" : "") : "UNKNOWN";
    }

    private String getAttackerNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getAttackerName(), cle.isAttackerIllusion());
    }

    private String getTargetNameCompiled(CombatLogEntry cle) {
        return compileName(cle.getTargetName(), cle.isTargetIllusion());
    }


    public boolean isFilterPlayer(Entity e) {
        int playerID = steamIDMap.get(filterSteamID).getPlayerID();
        int entityID = ClarityUtil.getEntityProperty(e, "m_iPlayerID");

        return (ClarityUtil.isPlayer(e) && entityID == playerID);
    }

}
