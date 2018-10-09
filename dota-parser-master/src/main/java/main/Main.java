package main;

import parser.MainParser;
import parser.Parser;

public class Main {

    public static void main(String[] args) throws Exception {
//        new MainParser("replay.dem.bz2", 76561198042528392L).start();
        //new MainParser("replay.dem.bz2").start();
        new MainParser("/cs/scratch/sy35/104070670/", 76561198064336398L, true).start();
        new MainParser("/cs/scratch/sy35/132309493/", 76561198092575221L, true).start();
        new MainParser("/cs/scratch/sy35/137193239/", 76561198097458967L, true).start();
        new MainParser("/cs/scratch/sy35/89269794/", 76561198049535522L, true).start();
        new MainParser("/cs/scratch/sy35/90892734/", 76561198051158462L, true).start();
//        new Parser(args[1], Integer.parseInt(args[2]));
    }

}

