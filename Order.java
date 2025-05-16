
/** 
 * Order.java
 * Each object embodies a customer's order ticket information
 */

import java.util.ArrayList;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Order extends Sendable {

    /* attributes */
    // The elements of the three parallel ArrayLists correspond by shared indices.
    private ArrayList<String> food; // order's food itemsD
    private ArrayList<Integer> quantity; // quantities for each food item (parallel to food)
    private ArrayList<String> special; // special requests for each food item (parallel to food)
    private boolean status; // whether the order has been completed
    private LocalDateTime dateAndTime; // date + time of the order's creation
    private LocalDateTime completionDate; // date + time when the order was completed
    private String studentID; // student ID associated with the order
    private int orderID; // unique ID assigned to each Order, useful for sorting and referencing
    private static int highestOrderID = 0; // keeps track of the highest order ID; new IDs increment from and update
                                           // this value
    public static final String DATATYPE = "ORDER"; // used to identify the class during deserialization

    /* constructors */
    public Order() // empty Order; for filling in details gradually
    {
        super.setDataType(DATATYPE);
        this.food = new ArrayList<String>();
        this.quantity = new ArrayList<Integer>();
        this.special = new ArrayList<String>();
        this.status = false; // order is incomplete
        this.dateAndTime = LocalDateTime.now(); // sets order date + time to current
        this.completionDate = null; // not completed yet
        this.studentID = ""; // no student ID by default
        this.orderID = highestOrderID + 1;
        updateHighestOrderID();
    }

    // pre-filled Order; particularly useful when converting saved (txt) data to
    // Orders
    public Order(ArrayList<String> food, ArrayList<Integer> quantity, ArrayList<String> special, Boolean status,
            LocalDateTime dateAndTime, int orderID) {
        super.setDataType(DATATYPE);
        this.food = food;
        this.quantity = quantity;
        this.special = special;
        this.status = status;
        this.dateAndTime = dateAndTime;
        this.completionDate = null; // will be set if status is true
        this.studentID = ""; // no student ID by default
        this.orderID = orderID;
        updateHighestOrderID();
        
        // If the order is already completed, set the completion date
        if (status) {
            this.completionDate = LocalDateTime.now();
        }
    }
    
    // Constructor with student ID
    public Order(ArrayList<String> food, ArrayList<Integer> quantity, ArrayList<String> special, Boolean status,
            LocalDateTime dateAndTime, String studentID, int orderID) {
        super.setDataType(DATATYPE);
        this.food = food;
        this.quantity = quantity;
        this.special = special;
        this.status = status;
        this.dateAndTime = dateAndTime;
        this.completionDate = null; // will be set if status is true
        this.studentID = studentID;
        this.orderID = orderID;
        updateHighestOrderID();
        
        // If the order is already completed, set the completion date
        if (status) {
            this.completionDate = LocalDateTime.now();
        }
    }

    /* mutator methods */

    /**
     * sets the Order's list of foods
     * 
     * @param food a list of foods (Strings)
     */
    public void setFood(ArrayList<String> food) {
        this.food = food;
    }

    /**
     * adds a food item (string) to the Order's list of foods
     * 
     * @param item individual food item to add to order
     */
    public void addOrderItem(String item) {
        this.food.add(item);
    }

    /**
     * used alongside addOrderItem to add the quantity of a food item
     * 
     * @param itemQuantity amount of a food item to add to order
     */
    public void addItemQuant(int itemQuantity) {
        this.quantity.add(itemQuantity);
    }

    /**
     * used alongside addOrderItem to add the special request of a food item
     * 
     * @param special special request of a food item to add to order
     */
    public void addSpecialRequest(String special) {
        this.special.add(special);
    }

    /**
     * sets the Order's list of food quantities (respective to food items in its
     * parallel list: food)
     * 
     * @param quantity a list of quantities (integers)
     */
    public void setQuantity(ArrayList<Integer> quantity) {
        this.quantity = quantity;
    }

    public void setSpecial(ArrayList<String> special) {
        this.special = special;
    }

    public void setStatus(boolean status) {
        this.status = status;
        
        // If the order is being marked as complete, set the completion date
        if (status) {
            this.completionDate = LocalDateTime.now();
        } else {
            this.completionDate = null; // Reset completion date if order is marked as incomplete
        }
    }
    
    /**
     * Sets the student ID for this order
     * 
     * @param studentID the student ID to set
     */
    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }
    
    /**
     * Gets the student ID for this order
     * 
     * @return the student ID
     */
    public String getStudentID() {
        return studentID;
    }
    
    /**
     * Gets the completion date for this order
     * 
     * @return the completion date, or null if the order is not completed
     */
    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    /**
     * Insertion Sort Algorithm of Order's Food Items
     */
    public void sortFoodByAlphabet() {
        for (int i = 1; i < food.size(); i++) {

            // Copy 1st unsorted element
            String unsortedFood = food.get(i);
            int unsortedQuantity = quantity.get(i);

            // marker holds last element of sorted array part,
            // remainder is unsorted
            int marker = i - 1;

            /*
             * start at marker, iterate backwards
             * swaps both food items and their respective quantities so that they are
             * inserted before other sorted elements
             * into their correct position based on sort type (alphabetical)
             */
            while (marker >= 0) {

                /*
                 * String comparison returns a negative (thus condition becomes true)
                 * if unsorted String alphabetically precedes marker food item
                 */
                if ((unsortedFood.compareTo(food.get(marker))) < 0) {
                    food.set(marker + 1, food.get(marker));
                    food.set(marker, unsortedFood);
                    quantity.set(marker + 1, quantity.get(marker));
                    quantity.set(marker, unsortedQuantity);
                } else {
                    // element is in correct place so stop iterating
                    marker = 0;
                }
                marker--;
            }
        }
    }

    public void sortFoodByQuantity() {
        for (int i = 1; i < quantity.size(); i++) {

            // Copy first unsorted element.
            String unsortedFood = food.get(i);
            int unsortedQuantity = quantity.get(i);

            // Create a marker to hold the last element of the sorted portion of the array,
            // the remaining portion of the array is unsorted
            int marker = i - 1;

            // Loop backwards through the sorted portion starting at marker

            while (marker >= 0) {
                if ((unsortedQuantity < (quantity.get(marker)))) {
                    food.set(marker + 1, food.get(marker));
                    food.set(marker, unsortedFood);
                    quantity.set(marker + 1, quantity.get(marker));
                    quantity.set(marker, unsortedQuantity);
                } else {
                    // element is in correct place so stop iterating
                    marker = 0;
                }
                marker--;
            }
        }
    }

    /**
     * Searches for a food item's index within an order's food list
     * Uses binary search algorithm to efficiently find the index
     *
     * @param item An order's food item to search for.
     * @return The index of the category if found, otherwise -1
     */
    public int searchItem(String item) {
        int leftBound = 0;
        int rightBound = food.size() - 1;
        int middleIndex = (leftBound + rightBound) / 2;
        while (leftBound <= rightBound) {
            if (food.get(middleIndex).compareToIgnoreCase(item) == 0) { // element at middle index is equal to search
                                                                        // value
                return middleIndex;
            } else if (food.get(middleIndex).compareToIgnoreCase(item) < 0) { // element at middle index is before
                                                                              // search value
                leftBound = middleIndex + 1;
                middleIndex = (leftBound + rightBound) / 2;
            } else if (food.get(middleIndex).compareToIgnoreCase(item) > 0) { // element at middle index is after search
                                                                              // value
                rightBound = middleIndex - 1;
                middleIndex = (leftBound + rightBound) / 2;
            }
        }
        return -1;
    }

    // accessor methods
    public ArrayList<String> getFood() {
        return food;
    }

    public ArrayList<Integer> getQuantity() {
        return quantity;
    }

    public ArrayList<String> getSpecial() {
        return special;
    }

    public Boolean getStatus() {
        return status;
    }

    public LocalDateTime getDateTime() {
        return dateAndTime;
    }

    public int getID() {
        return orderID;
    }

    // Returns highestOrderID of all Orders
    public static int getHighestOrderID() {
        return highestOrderID;
    }

    // updates the highestOrderID with this Order's ID if it is greater
    public void updateHighestOrderID() {
        highestOrderID = Math.max(orderID, highestOrderID);
    }

    public String toString()
    // used in converting Orders to text to be stored in the database txt
    // ("orders.txt")
    {
        return "Order: " + "food=" + food.toString().replaceAll("[\\[\\]]", "")
                + "; quantity=" + quantity.toString().replaceAll("[\\[\\]]", "")
                + "; special=" + special.toString().replaceAll("[\\[\\]]", "") 
                + "; status=" + status 
                + "; dateAndTime=" + dateAndTime 
                + "; completionDate=" + completionDate
                + "; studentID=" + studentID 
                + "; ID=" + orderID;
    }

    public boolean equals(Order other) {
        return this.toString().equals(other.toString());
    }
}
