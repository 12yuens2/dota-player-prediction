package parser.game;

import parser.MainParser;
import parser.Parser;
import parser.PlayerData;
import skadistats.clarity.decoder.Util;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.StringTable;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.stringtables.StringTables;
import util.ClarityUtil;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class GameParser extends Parser {

    private PrintWriter itemWriter;
    private int startItemGameTick;
    private boolean wroteStartingItems;

    public GameParser(long filterSteamID, String outputPath) {
        super(filterSteamID, outputPath);

        this.startItemGameTick = -1;
        this.wroteStartingItems = false;
    }

    public boolean writeItems(Context ctx, PlayerData pd, Inventory.Period period) {
        Entity pr = ClarityUtil.getEntity(ctx, "CDOTA_PlayerResource");
        if (pr != null && pd != null) {
            int playerID = pd.getPlayerID();
            int id = ClarityUtil.resolveValue(ctx, "CDOTA_PlayerResource", "m_vecPlayerTeamData.%i.m_hSelectedHero", playerID, 0, 0);
            Entity player = ctx.getProcessor(Entities.class).getByHandle(id);
            if (player != null) {
                Inventory heroInventory = getHeroInventory(ctx, player, period);
                writeInventory(heroInventory);
                return true;
            }
        }
        return false;
    }

    private void writeInventory(Inventory inventory) {
        System.out.println("write inventory");
        itemWriter.write(inventory.toCSV(filterSteamID) + "\n");
        itemWriter.flush();
    }

    private Inventory getHeroInventory(Context ctx, Entity e, Inventory.Period period) {
        Inventory heroInventory = new Inventory(period);

        // Inventory
        for (int i = 0; i < 6; i++) {
            heroInventory.setInventoryItem(getItem(ctx, e, i), i);
        }

        // Backpack
        for (int i = 0; i < 3; i++) {
            heroInventory.setBackpackItem(getItem(ctx, e, i + Inventory.BACKPACK_OFFSET), i);
        }

        // Stash
        for (int i = 0; i < 8; i++) {
            heroInventory.setStashItem(getItem(ctx, e, i + Inventory.STASH_OFFSET), i);
        }

        return heroInventory;
    }

    private Item getItem(Context ctx, Entity e, int index) {
        try {
            StringTable entityNames = ctx.getProcessor(StringTables.class).forName("EntityNames");
            Entities entities = ctx.getProcessor(Entities.class);

            int hItem = e.getProperty("m_hItems." + Util.arrayIdxToString(index));
            Entity eItem = entities.getByHandle(hItem);

            if (eItem != null && entityNames != null) {
                String itemName = entityNames.getNameByIndex(eItem.getProperty("m_pEntity.m_nameStringableIndex"));
                return new Item(hItem, itemName);
            }
        } catch (Exception ex) {
            System.err.println("Get item " + index + " : " + ex);
        }

        return Item.emptyItem();
    }

    public void initWriter() throws FileNotFoundException {
        itemWriter = new PrintWriter(outputPath + "-iteminfo.csv");
        itemWriter.write(Inventory.headers() + "\n");
        itemWriter.flush();
    }

    public void writeStartingItems(Context ctx, int gameTick, PlayerData pd) {
        if (startItemGameTick == -1) {
            startItemGameTick = gameTick + (30 * MainParser.TICK_RATE);
            return;
        }

        if (startItemGameTick == gameTick) {
            if (writeItems(ctx, pd, Inventory.Period.START_GAME)) {
                wroteStartingItems = true;
            } else {

                // Increment start tick to try again on next tick
                startItemGameTick++;
            }
        }

    }

    public void closeWriter() {
        if (itemWriter != null) {
            itemWriter.flush();
            itemWriter.close();
        }
    }


    public boolean writtenStartingItems() {
        return wroteStartingItems;
    }
}
