package parser;

import skadistats.clarity.processor.runner.Context;
import util.ClarityUtil;


public class StatParser extends Parser {

    public static final String PLAYER_RESOURCE = "CDOTA_PlayerResource";
    public static final String TEAM_DATA = "CDOTA_Data";

    public StatParser(long filterSteamID) {
        super(filterSteamID);
    }


    public void printStats(Context ctx, int id, String teamName) {
        if (teamName == "Dire") {
            // Offset id to get team data
            id -= 5;
        }
        String teamData = TEAM_DATA + teamName;
        int kills = getPlayerStat(ctx, id, PLAYER_RESOURCE, "m_vecPlayerTeamData.%i.m_iKills");
        int assists = getPlayerStat(ctx, id, PLAYER_RESOURCE, "m_vecPlayerTeamData.%i.m_iAssists");
        int deaths = getPlayerStat(ctx, id, PLAYER_RESOURCE, "m_vecPlayerTeamData.%i.m_iDeaths");
        int totalGold = getPlayerStat(ctx, id, teamData, "m_vecDataTeam.%i.m_iTotalEarnedGold");
        int totalXP = getPlayerStat(ctx, id, teamData, "m_vecDataTeam.%i.m_iTotalEarnedXP");
        int lastHits = getPlayerStat(ctx, id, teamData, "m_vecDataTeam.%i.m_iLastHitCount");
        int denies = getPlayerStat(ctx, id, teamData, "m_vecDataTeam.%i.m_iDenyCount");

        System.out.println(String.format(
                "Kills: %d, Assists: %d, Deaths: %d, Gold: %d, XP: %d, CS: %d, Denies: %d",
                kills, assists, deaths, totalGold, totalXP, lastHits, denies));
    }

    private <T> T getPlayerStat(Context ctx, int id, String entity, String stat) {
        return ClarityUtil.resolveValue(ctx, entity, stat, id, 0, 0);
    }


}
