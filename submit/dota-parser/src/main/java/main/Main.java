package main;

import parser.MainParser;

public class Main {

    public static void main(String[] args) {
        new MainParser(args[0], 0, true).start();
    }

}

