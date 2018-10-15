package main;

import parser.MainParser;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        new MainParser(args[0], 0, true).start();
    }

}

