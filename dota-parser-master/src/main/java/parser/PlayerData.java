package parser;

public class PlayerData {

    private final int id, heroID;
    private final long steamID;

    public PlayerData(int id, int heroID, long steamID) {
        this.id = id;
        this.heroID = heroID;
        this.steamID = steamID;
    }

    public int getID() {
        return id;
    }

    public int getHeroID() {
        return heroID;
    }

    public long getSteamID() {
        return steamID;
    }


}
