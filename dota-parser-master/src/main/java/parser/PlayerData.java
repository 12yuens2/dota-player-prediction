package parser;

import skadistats.clarity.model.Entity;
import util.ClarityUtil;

public class PlayerData {

    private int id, heroID, selectedHero;
    private long steamID;
    private String teamName;


    public PlayerData(int id) {
        this.id = id;
    }

    public PlayerData(int id, int heroID, int selectedHero, int teamNum, long steamID) {
        this.id = id;
        this.heroID = heroID;
        this.selectedHero = selectedHero;
        this.steamID = steamID;

        this.teamName = ClarityUtil.getTeamName(teamNum);
    }

    public int getPlayerID() {
        return id;
    }

    public int getHeroID() {
        return heroID;
    }

    public void setHeroID(int heroID) {
        this.heroID = heroID;
    }


    public int getSelectedHero() { return selectedHero; }

    public void setSelectedHero(int selectedHero) { this.selectedHero = selectedHero; }


    public long getSteamID() { return steamID; }

    public void setSteamID(long steamID) {
        this.steamID = steamID;
    }


    public String getTeamName() {
        return teamName;
    }

}
