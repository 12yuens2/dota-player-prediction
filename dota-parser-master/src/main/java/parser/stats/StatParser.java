package parser.stats;

import parser.Parser;
import parser.PlayerData;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.wire.common.proto.DotaUserMessages;
import util.ClarityUtil;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;


public class StatParser extends Parser {

    public static final String PLAYER_RESOURCE = "CDOTA_PlayerResource";
    public static final String TEAM_DATA = "CDOTA_Data";

    public float duration;

    private HashMap<Long, PlayerStats> statsMap;

    public StatParser(long filterSteamID) {
        super(filterSteamID);

        statsMap = new HashMap<>();
    }


    public String getStats(Context ctx, int id, String teamName) {
        if (teamName == "Dire") {
            // Offset id to get team data
            id -= 5;
        }

        if (duration == 0) {
            duration = (float) (gameTick/30.0) - 90;
            System.out.println("Using gametick " + gameTick + "for duation " + duration);
        }
        String teamData = TEAM_DATA + teamName;
        int kills = getPlayerStat(ctx, id, PLAYER_RESOURCE, "m_vecPlayerTeamData.%i.m_iKills");
        int assists = getPlayerStat(ctx, id, PLAYER_RESOURCE, "m_vecPlayerTeamData.%i.m_iAssists");
        int deaths = getPlayerStat(ctx, id, PLAYER_RESOURCE, "m_vecPlayerTeamData.%i.m_iDeaths");
        int totalGold = getPlayerStat(ctx, id, teamData, "m_vecDataTeam.%i.m_iTotalEarnedGold");
        int totalXP = getPlayerStat(ctx, id, teamData, "m_vecDataTeam.%i.m_iTotalEarnedXP");
        int lastHits = getPlayerStat(ctx, id, teamData, "m_vecDataTeam.%i.m_iLastHitCount");
        int denies = getPlayerStat(ctx, id, teamData, "m_vecDataTeam.%i.m_iDenyCount");

        StringBuilder sb = new StringBuilder();
	sb.append(String.format("%d,", filterSteamID));
        sb.append(String.format("%d,%d,%d,%d,%d,%d,%d,", kills, assists, deaths, totalGold, totalXP, lastHits, denies));
        sb.append(String.format("%f,%f,%f,", totalGold/duration, totalXP/duration, lastHits/duration));
        sb.append(statsMap.get(filterSteamID).getStats(duration));

        return sb.toString();
    }

    public void writeStats(Context ctx, PlayerData pd, String outputFilename) throws FileNotFoundException {
        int playerID = pd.getPlayerID();
        String teamName = pd.getTeamName();
        PrintWriter writer = new PrintWriter(outputFilename);
        writer.write("steamid,kills,assists,deaths,gold,xp,cs,denies,gold/min,xp/min,cs/min,apm,moves(p),moves(t),attacks(p),attacks(t),casts(p),casts(t),casts(n),holds(p)\n");
        writer.write(getStats(ctx, playerID, teamName));
        writer.flush();

        writer.close();
    }

    public void parseUnitOrder(long steamid, DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
        if (statsMap.containsKey(steamid)) {
            PlayerStats stats = statsMap.get(steamid);
            stats.update(msg);
        } else {
            PlayerStats stats = new PlayerStats();
            stats.update(msg);
            statsMap.put(steamid, stats);
        }
    }

    private <T> T getPlayerStat(Context ctx, int id, String entity, String stat) {
        return ClarityUtil.resolveValue(ctx, entity, stat, id, 0, 0);
    }


}
