package parser;

import skadistats.clarity.model.Entity;

public class PlayerData {

    private int id, heroID, selectedHero;
    private long steamID;


    public PlayerData(int id) {
        this.id = id;
    }

    public PlayerData(int id, int heroID, int selectedHero, long steamID) {
        this.id = id;
        this.heroID = heroID;
        this.selectedHero = selectedHero;
        this.steamID = steamID;
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



}
