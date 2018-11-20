package parser.game;

public class Item {

    private int itemID;
    private String itemName;

    // Used for observer wards
    private int num_charges;

    //Used for sentry wards
    private int num_second_charges;


    public static Item emptyItem() {
        return new Item(-1, "item_empty");
    }

    public Item(int id, String name) {
        this.itemID = id;
        this.itemName = name;
    }

    public String getItemName() {
        return itemName;
    }
}
