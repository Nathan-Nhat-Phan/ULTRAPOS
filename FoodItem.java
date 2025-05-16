
/** 
 * FoodItem.java
 * A FoodItem (of a Category) in the menu that holds modifiers
 */

import java.util.ArrayList;
// Assuming Menu class is in the same package or accessible.
// If not, an import statement like: import com.yourpackage.Menu; would be needed.

public class FoodItem extends Sendable {
    public static final String DATATYPE = "FOOD_ITEM"; // used to identify the class during deserialization
    private String name; // name of food item
    private double basePrice; // food item's price (without modifiers)
    private ArrayList<Modifier> modifiersList; // list of modifiers for this food item
    private int categoryID; // ID of host category that this food item composes (used to find the food item)
    private int foodItemID; // unique ID assigned to each foodItem, useful for sorting and referencing
    private static int highestFoodItemID = 0; // highest food item ID number, from which new IDs are incremented from

    // Stock management fields
    private String primaryStockComponentId; // Links to a StockableComponent for the main item's stock
    private ArrayList<RecipeItem> recipe;   // Defines other StockableComponents needed to make this FoodItem

    public FoodItem() // only use while deserializing
    {
        super.setDataType(DATATYPE);
        this.name = "";
        this.categoryID = Category.getHighestCategoryID(); // Assuming Category.getHighestCategoryID() is appropriate
        this.modifiersList = new ArrayList<Modifier>();
        this.recipe = new ArrayList<RecipeItem>();
        // primaryStockComponentId will be null by default
    }

    public FoodItem(int categoryID) {
        super.setDataType(DATATYPE);
        this.name = "";
        this.categoryID = categoryID;
        this.foodItemID = highestFoodItemID + 1;
        updateHighestFoodItemID();
        this.modifiersList = new ArrayList<Modifier>();
        this.recipe = new ArrayList<RecipeItem>();
        // primaryStockComponentId will be null by default
    }

    public FoodItem(int categoryID, String name) {
        super.setDataType(DATATYPE);
        this.name = name;
        this.categoryID = categoryID;
        this.foodItemID = highestFoodItemID + 1;
        updateHighestFoodItemID();
        this.modifiersList = new ArrayList<Modifier>();
        this.recipe = new ArrayList<RecipeItem>();
        // primaryStockComponentId will be null by default
    }

    public ArrayList<Modifier> getModifiers() {
        return modifiersList;
    }

    public int getID() {
        return foodItemID;
    }

    public int getCategoryID() {
        return categoryID;
    }

    // Calculates price with additional cost from modifiers
    public double getPrice(ArrayList<Modifier> appliedMods) {
        double price = this.basePrice;
        for (Modifier m : appliedMods) {
            price += m.getAddCost();
        }
        return price;
    }

    public double getPrice() // gets the base price
    {
        double price = this.basePrice;
        return price;
    }

    public static int getHighestFoodItemID() {
        return highestFoodItemID;
    }

    public void updateHighestFoodItemID() {
        highestFoodItemID = Math.max(foodItemID, highestFoodItemID);
    }

    public int searchByModifierID(int modifierID) {
        int left_bound = 0;
        int right_bound = modifiersList.size() - 1;
        int middleIndex = (left_bound + right_bound) / 2;
        while (left_bound <= right_bound) {
            // System.out.println(left_bound + ", " + right_bound);
            if (modifiersList.get(middleIndex).getID() == (modifierID)) {
                return middleIndex;
            } else if (modifiersList.get(middleIndex).getID() < (modifierID)) {
                left_bound = middleIndex + 1;
                middleIndex = (left_bound + right_bound) / 2;
            } else if (modifiersList.get(middleIndex).getID() > (modifierID)) {
                right_bound = middleIndex - 1;
                middleIndex = (left_bound + right_bound) / 2;
            }
        }
        return -1;
    }

    public boolean equals(FoodItem other) {
        return this.name.equals(other.name) &&
                this.basePrice == other.basePrice &&
                this.modifiersList.equals(other.modifiersList);
    }

    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public void addModifier(Modifier modifier) {
        modifiersList.add(modifier);
    }

    // Getters and Setters for stock management fields
    public String getPrimaryStockComponentId() {
        return primaryStockComponentId;
    }

    public void setPrimaryStockComponentId(String primaryStockComponentId) {
        this.primaryStockComponentId = primaryStockComponentId;
    }

    public ArrayList<RecipeItem> getRecipe() {
        if (this.recipe == null) { // Defensive initialization
            this.recipe = new ArrayList<>();
        }
        return recipe;
    }

    public void setRecipe(ArrayList<RecipeItem> recipe) {
        this.recipe = recipe;
    }

    public void addRecipeItem(RecipeItem item) {
        if (this.recipe == null) {
            this.recipe = new ArrayList<>();
        }
        this.recipe.add(item);
    }

    public void removeRecipeItem(RecipeItem item) {
        if (this.recipe != null) {
            this.recipe.remove(item);
        }
    }
    
    // It's important that the FoodItemDeserializer is updated to handle these new fields,
    // especially for loading older menu.txt files where these fields might be missing.
    // Gson will typically set them to null or default values if missing in JSON,
    // which is handled by the constructors and getters.

    /**
     * Calculates the maximum quantity of this food item that can be made based on available stock
     * of its primary component and recipe ingredients.
     *
     * @param menu The Menu object, used to access the global list of StockableComponents.
     * @return The maximum makeable quantity. Returns Integer.MAX_VALUE if stock is not tracked
     *         or effectively unlimited based on defined components.
     */
    public int calculateMaxMakeableQuantity(Menu menu) {
        if (menu == null) {
            // Cannot determine stock without menu context
            return 0; 
        }

        int maxBasedOnPrimary = Integer.MAX_VALUE;
        if (this.primaryStockComponentId != null && !this.primaryStockComponentId.isEmpty()) {
            StockableComponent primaryComponent = menu.getStockableComponentById(this.primaryStockComponentId);
            if (primaryComponent != null && primaryComponent.isTrackStock()) {
                maxBasedOnPrimary = primaryComponent.getStockQuantity();
            } else if (primaryComponent == null || !primaryComponent.isTrackStock()) {
                // If primary component is defined but not found or not tracked,
                // and there's no recipe, it implies it's not makeable if we expect it to be tracked.
                // However, if it's just not tracked, it doesn't limit.
                // For simplicity, if it's specified but not tracked, it doesn't impose a limit from this path.
                // The recipe might still impose a limit. If no recipe, then it's "unlimited".
            }
        }

        int maxBasedOnRecipe = Integer.MAX_VALUE;
        ArrayList<RecipeItem> currentRecipe = getRecipe(); // Ensures recipe is initialized

        if (currentRecipe != null && !currentRecipe.isEmpty()) {
            boolean recipeHasTrackedItems = false;
            for (RecipeItem recipeItem : currentRecipe) {
                if (recipeItem.getQuantityPerItem() <= 0) {
                    continue; // This ingredient doesn't consume stock, skip.
                }
                recipeHasTrackedItems = true; // Mark that the recipe has at least one consuming item.

                StockableComponent ingredientComponent = menu.getStockableComponentById(recipeItem.getComponentId());

                if (ingredientComponent == null || !ingredientComponent.isTrackStock()) {
                    // If a required, tracked ingredient is missing or not tracked for stock,
                    // then none can be made based on this recipe.
                    maxBasedOnRecipe = 0;
                    break; 
                }

                // If quantityPerItem is 0, it would lead to division by zero. Already handled.
                int canMakeWithIngredient = ingredientComponent.getStockQuantity() / recipeItem.getQuantityPerItem();
                maxBasedOnRecipe = Math.min(maxBasedOnRecipe, canMakeWithIngredient);
            }
             if (!recipeHasTrackedItems) { // Recipe exists but all items have quantity 0 or less
                maxBasedOnRecipe = Integer.MAX_VALUE; // Effectively no limit from recipe
            }
        } else {
            // No recipe, so no limit from recipe.
            maxBasedOnRecipe = Integer.MAX_VALUE;
        }


        // Determine final max makeable quantity
        if (primaryStockComponentId == null && (currentRecipe == null || currentRecipe.isEmpty())) {
            // Not tracked by primary component and no recipe items
            return Integer.MAX_VALUE; 
        }
        
        // If maxBasedOnRecipe is 0 due to a missing/untracked ingredient, that's the limit.
        // Otherwise, it's the minimum of what primary stock allows and what recipe stock allows.
        return Math.min(maxBasedOnPrimary, maxBasedOnRecipe);
    }
}
