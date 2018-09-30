package parser;

import skadistats.clarity.model.Entity;

public class PlayerData {

    private int id, heroID;
    private long steamID;


    public PlayerData(int id) {
        this.id = id;
    }

    public PlayerData(int id, int heroID, long steamID) {
        this.id = id;
        this.heroID = heroID;
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


    public long getSteamID() {
        return steamID;
    }

    public void setSteamID(long steamID) {
        this.steamID = steamID;
    }



}
