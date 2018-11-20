package parser;

public abstract class Parser {

    protected long filterSteamID;
    protected int gameTick;


    public Parser(long filterSteamID) {
        this.filterSteamID = filterSteamID;
        this.gameTick = 0;
    }

    public void tick() {
        gameTick++;
    }


}
