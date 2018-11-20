package parser.game;

public class Inventory {


    public enum Period { START_GAME, MID_GAME, END_GAME }
    public static int BACKPACK_OFFSET = 6;
    public static int STASH_OFFSET = 9;

    public static int INVENTORY_SIZE = 6;
    public static int BACKPACK_SIZE = 3;
    public static int STASH_SIZE = 8;

    private Item[] inventory, backpack, stash;
    private Period period;

    public Inventory() {
        this.inventory = new Item[INVENTORY_SIZE];
        this.backpack = new Item[BACKPACK_SIZE];
        this.stash = new Item[STASH_SIZE];
    }

    public Inventory(Period period) {
        this();
        this.period = period;
    }

    public static String headers() {
        StringBuilder sb = new StringBuilder();

        sb.append("steamid,");
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            sb.append(String.format("inv_%d,", i));
        }

        for (int i = 0; i < BACKPACK_SIZE; i++) {
            sb.append(String.format("backpack_%d,", i));
        }

        for (int i = 0; i < STASH_SIZE; i++) {
            sb.append(String.format("stash_%d,", i));
        }
        sb.append("period");

        return sb.toString();
    }


    public String toCSV(long filterSteamID) {
        StringBuilder sb = new StringBuilder();

        sb.append(filterSteamID + ",");
        for (Item i : inventory) {
            sb.append(i.getItemName() + ",");
        }

        for (Item i : backpack) {
            sb.append(i.getItemName() + ",");
        }

        for (Item i : stash) {
            sb.append(i.getItemName() + ",");
        }
        sb.append(period.toString());

        return sb.toString();

    }



    public Item[] getInventory() {
        return inventory;
    }

    public Item getInventory(int i) {
        return i < inventory.length ? inventory[i] : null;
    }

    public void setInventoryItem(Item item, int i) {
        inventory[i] = item;
    }


    public Item[] getBackpack() {
        return backpack;
    }

    public Item getBackpack(int i) {
        return i < backpack.length ? backpack[i] : null;
    }

    public void setBackpackItem(Item item, int i) {
        backpack[i] = item;
    }


    public Item[] getStash() {
        return stash;
    }

    public Item getStash(int i) {
        return i < stash.length ? stash[i] : null;
    }

    public void setStashItem(Item item, int i) {
        stash[i] = item;
    }

}
