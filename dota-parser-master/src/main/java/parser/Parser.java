package parser;

import java.io.FileNotFoundException;

public abstract class Parser {

    protected long filterSteamID;
    protected int gameTick;
    protected String outputPath;


    public Parser(long filterSteamID) {
        this.filterSteamID = filterSteamID;
        this.gameTick = 0;
    }

    public Parser(long filterSteamID, String outputPath) {
        this(filterSteamID);
        this.outputPath = outputPath;
    }

    public void tick() {
        gameTick++;
    }

    public abstract void initWriter() throws FileNotFoundException;

    public abstract void closeWriter();


}
