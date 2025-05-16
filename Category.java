
/** 
 * Category.java
 * A category in the menu that holds food items
 */

import java.util.ArrayList;

public class Category extends Sendable {
    public static final String DATATYPE = "CATEGORY"; // used to identify the class during deserialization
    private String name; // name of category
    private ArrayList<FoodItem> foodItemsList; // list of food items within this category
    private int categoryID; // unique ID assigned to each Category, useful for sorting and referencing
    private static int highestCategoryID = 0; // highest category ID number, from which new IDs are incremented from

    public Category() { // constructor used during deserialization
        super.setDataType(DATATYPE);
        this.name = "";
        foodItemsList = new ArrayList<FoodItem>();
    }

    public Category(String name) {
        super.setDataType(DATATYPE);
        this.categoryID = highestCategoryID + 1;
        updateHighestCategoryID();
        this.name = name;
        foodItemsList = new ArrayList<FoodItem>();
    }

    public boolean equals(Category other) {
        return this.name.equals(other.name) &&
                this.foodItemsList.equals(other.foodItemsList);
    }

    public String toString() {
        return name;
    }

    public ArrayList<FoodItem> getFoodItems() {
        return foodItemsList;
    }

    public int getID() {
        return categoryID;
    }

    public static int getHighestCategoryID() {
        return highestCategoryID;
    }

    public void updateHighestCategoryID() {
        highestCategoryID = Math.max(categoryID, highestCategoryID);
    }

    public int searchByFoodItemID(int foodItemID) {
        int left_bound = 0;
        int right_bound = foodItemsList.size() - 1;
        int middleIndex = (left_bound + right_bound) / 2;
        while (left_bound <= right_bound) {
            // System.out.println(left_bound + ", " + right_bound);
            if (foodItemsList.get(middleIndex).getID() == (foodItemID)) {
                return middleIndex;
            } else if (foodItemsList.get(middleIndex).getID() < (foodItemID)) {
                left_bound = middleIndex + 1;
                middleIndex = (left_bound + right_bound) / 2;
            } else if (foodItemsList.get(middleIndex).getID() > (foodItemID)) {
                right_bound = middleIndex - 1;
                middleIndex = (left_bound + right_bound) / 2;
            }
        }
        return -1;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addFoodItem(FoodItem foodItem) {
        foodItemsList.add(foodItem);
    }
}
