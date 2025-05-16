
/** 
 * Modifier.java
 * A Modifier (of a FoodItem) in the menu
 * Terminology: Modifiers = Special Requests
 */

public class Modifier extends Sendable {
    public static final String DATATYPE = "MODIFIER"; // used to identify the class during deserialization
    private String name; // modifier's name
    private double additionalCost; // the cost a modifier adds to a food item
    private int categoryID; // ID of host category that this modifier's host food item composes (used to find the modifier)
    private int foodItemID; // ID of host food item that this modifier composes (used to find the modifier)
    private int modifierID; // unique ID assigned to each foodItem, useful for sorting and referencing;
    private static int highestModifierID = 0; // highest modifier ID number, from which new IDs are incremented from

    // Stock management field
    private String stockComponentId; // Links to a StockableComponent for this modifier's stock
    private int quantityEffect; // How many units of the stockComponentId are affected (+ve for consumption, -ve for reduction)

    public Modifier() // only use while deserializing
    {
        super.setDataType(DATATYPE);
        this.name = "";
        this.categoryID = Category.getHighestCategoryID(); // Assuming Category.getHighestCategoryID() is appropriate
        this.foodItemID = FoodItem.getHighestFoodItemID(); // Assuming FoodItem.getHighestFoodItemID() is appropriate
        // stockComponentId will be null by default
        this.quantityEffect = 0; // Default to no effect
    }

    public Modifier(int categoryID, int foodItemID) {
        super.setDataType(DATATYPE);
        this.name = "";
        this.categoryID = categoryID;
        this.foodItemID = foodItemID;
        this.additionalCost = 0;
        this.modifierID = highestModifierID + 1;
        updateHighestModifierID();
        // stockComponentId will be null by default
        this.quantityEffect = 0; // Default to no effect
    }

    public Modifier(int categoryID, int foodItemID, String name) {
        super.setDataType(DATATYPE);
        this.name = name;
        this.categoryID = categoryID;
        this.foodItemID = foodItemID;
        this.additionalCost = 0;
        this.modifierID = highestModifierID + 1;
        updateHighestModifierID();
        // stockComponentId will be null by default
        this.quantityEffect = 0; // Default to no effect
    }

    public double getAddCost() {
        return additionalCost;
    }

    public int getID() {
        return foodItemID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public int getFoodItemID() {
        return foodItemID;
    }

    public String getDataType() {
        return DATATYPE;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddCost(double additionalCost) {
        this.additionalCost = additionalCost;
    }

    public boolean equals(Modifier other) {
        if (other == null) return false;
        return this.name.equals(other.name) &&
                this.additionalCost == other.additionalCost &&
                (this.stockComponentId == null ? other.stockComponentId == null : this.stockComponentId.equals(other.stockComponentId)) &&
                this.quantityEffect == other.quantityEffect;
    }

    public String toString() {
        return name;
    }

    public static int getHighestModifierID() {
        return highestModifierID;
    }

    public void updateHighestModifierID() {
        highestModifierID = Math.max(modifierID, highestModifierID);
    }

    // Getter and Setter for stockComponentId
    public String getStockComponentId() {
        return stockComponentId;
    }

    public void setStockComponentId(String stockComponentId) {
        this.stockComponentId = stockComponentId;
    }

    // Getter and Setter for quantityEffect
    public int getQuantityEffect() {
        return quantityEffect;
    }

    public void setQuantityEffect(int quantityEffect) {
        this.quantityEffect = quantityEffect;
    }

    // It's important that the ModifierDeserializer is updated to handle this new field,
    // especially for loading older menu.txt files where this field might be missing.
    // Gson will typically set it to null if missing in JSON, which is handled by the constructor.
}
