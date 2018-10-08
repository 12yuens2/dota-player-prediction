package main;

import parser.MainParser;
import parser.Parser;

public class Main {

    public static void main(String[] args) throws Exception {
//        new MainParser("replay.dem.bz2", 76561198042528392L).start();
        new MainParser("replay.dem.bz2").start();
//        new Parser(args[1], Integer.parseInt(args[2]));
    }

}

