
/** 
 * RetailButton.java
 * Selectable buttons in RetailView for order input
 * Can input categories, food items, or modifiers
 */

import java.awt.Color;
import javax.swing.UIManager; // Added for UIManager
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.TableModel;

public class RetailButton extends JToggleButton implements ActionListener {
    private static final int CATEGORY = 0;
    private static final int FOOD_ITEM = 1;
    private static final int MODIFIER = 2;
    private static final int LOW_STOCK_THRESHOLD = 5; // Threshold for "low stock" warning

    private final int type;
    private RetailView retailPanel;
    private Category category;
    private FoodItem foodItem;
    private Modifier modifier;
    private int categoryIndex;
    private int foodItemIndex;
    private int modifierIndex;

    public RetailButton(String text, Category category, int categoryIndex, RetailView retailPanel) {
        this.type = CATEGORY;
        this.retailPanel = retailPanel;
        this.categoryIndex = categoryIndex;
        setText(text);
        setFocusable(false);
        this.category = category;
        addActionListener(this);
    }

    public RetailButton(String text, FoodItem foodItem, int categoryIndex, int foodItemIndex, RetailView retailPanel) {
        this.type = FOOD_ITEM;
        this.retailPanel = retailPanel;
        this.categoryIndex = categoryIndex;
        this.foodItemIndex = foodItemIndex;
        setText(text);
        setFocusable(false);
        this.foodItem = foodItem;
        addActionListener(this);
        updateStockBasedAppearance(); // Update appearance based on stock
    }

    public RetailButton(String text, Modifier modifier, int categoryIndex, int foodItemIndex, int modifierIndex,
            RetailView retailPanel) {
        this.type = MODIFIER;
        this.retailPanel = retailPanel;
        this.categoryIndex = categoryIndex;
        this.foodItemIndex = foodItemIndex;
        this.modifierIndex = modifierIndex;
        setText(text);
        setFocusable(false);
        this.modifier = modifier;
        addActionListener(this);
        updateStockBasedAppearance(); // Update appearance for modifiers too
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTable table = retailPanel.getOrderDisplayTable();
        TableModel model = retailPanel.getOrderDisplayTableModel();
        int currentRow = retailPanel.getCurrentRowInOrder();
        Menu menu = retailPanel.getGUI().getPOS().getMenu();
        if (type == CATEGORY) {
            retailPanel.renderFoodItems(categoryIndex);
            model.setValueAt(null, currentRow, 0);
            model.setValueAt(null, currentRow, 3);
            model.setValueAt(null, currentRow, 4);
            model.setValueAt(null, currentRow, 2);
            model.setValueAt(null, currentRow, 1);
            model.setValueAt(null, currentRow, 5);
        } else if (type == FOOD_ITEM) {
            Category category = menu.getCategories().get(categoryIndex);
            FoodItem foodItem = category.getFoodItems().get(foodItemIndex);
            System.out.println(foodItem);
            model.setValueAt(foodItem, currentRow, 0);
            model.setValueAt(foodItem.getPrice(), currentRow, 3);
            model.setValueAt(foodItem.getPrice(), currentRow, 4);
            model.setValueAt("", currentRow, 2);
            model.setValueAt(1, currentRow, 1);
            retailPanel.renderModifiers(categoryIndex, foodItemIndex);
        } else if (type == MODIFIER) {
            Category category = menu.getCategories().get(categoryIndex);
            FoodItem foodItem = category.getFoodItems().get(foodItemIndex);
            model.setValueAt(foodItem.getPrice(retailPanel.getSelectedModifiers()), currentRow, 4); // gets food item's
                                                                                                    // price with
                                                                                                    // modifiers
            retailPanel.renderConfirmFoodItemPanel();
            // in the special column, list all the modifiers
            model.setValueAt(retailPanel.getSelectedModifiers().toString().replaceAll("[\\[\\]]", ""), currentRow, 2);
            NewLinesRowHeightAdjuster.adjustRowHeightForRow(table, currentRow);
        }
    }

    public Modifier getModifier() {
        return modifier;
    }

    public int getCategoryIndex() {
        // This should only be called on a button of type CATEGORY or FOOD_ITEM or MODIFIER
        // as they all store categoryIndex.
        return categoryIndex;
    }

    // Optional: Add getters for foodItemIndex and modifierIndex if needed elsewhere
    public int getFoodItemIndex() {
        return foodItemIndex;
    }

    public int getModifierIndex() {
        return modifierIndex;
    }

    public void updateStockBasedAppearance() {
        Menu menu = retailPanel.getGUI().getPOS().getMenu();
        if (menu == null) {
            return; // Cannot update without menu
        }

        if (this.type == FOOD_ITEM && this.foodItem != null && this.retailPanel != null) {
            int maxMakeable = foodItem.calculateMaxMakeableQuantity(menu);

            if (maxMakeable == 0) {
                setBackground(Color.LIGHT_GRAY);
                setEnabled(false);
            } else if (maxMakeable <= LOW_STOCK_THRESHOLD) {
                setBackground(Color.ORANGE);
                setEnabled(true);
            } else {
                setBackground(UIManager.getColor("ToggleButton.background"));
                setEnabled(true);
            }
        } else if (this.type == MODIFIER && this.modifier != null && this.retailPanel != null) {
            if (modifier.getStockComponentId() != null && !modifier.getStockComponentId().isEmpty()) {
                StockableComponent sc = menu.getStockableComponentById(modifier.getStockComponentId());
                if (sc != null && sc.isTrackStock()) {
                    if (sc.getStockQuantity() <= 0) {
                        setBackground(Color.LIGHT_GRAY);
                        // For modifiers, we might not want to disable them entirely even if out of stock,
                        // as the user might still want to select it (e.g., to indicate a preference even if unavailable).
                        // setEnabled(false); 
                    } else if (sc.getStockQuantity() <= LOW_STOCK_THRESHOLD) {
                        setBackground(Color.ORANGE);
                        setEnabled(true);
                    } else {
                        setBackground(UIManager.getColor("ToggleButton.background"));
                        setEnabled(true);
                    }
                } else { // Component not found or not tracked
                    setBackground(UIManager.getColor("ToggleButton.background"));
                    setEnabled(true);
                }
            } else { // No stock component linked to modifier
                setBackground(UIManager.getColor("ToggleButton.background"));
                setEnabled(true);
            }
        }
    }
}
