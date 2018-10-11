package main;

import parser.MainParser;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length % 2 != 0 || args.length < 2) {
            System.out.println("Got wrong number of args");
            System.out.println(args.length);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i+=2 )  {
            String replayFolder = args[i];
            long steamID = Long.parseLong(args[i+1]);

            new MainParser(replayFolder, steamID, true).start();
        }
    }

}

