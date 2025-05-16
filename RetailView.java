
/** 
 * RetailView.java
 * View Panel for retail (cash register) use
 * Uses: Inputting orders to be sent to the kitchen
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap; // Added for stock tracking
import java.util.List;    // Added for stock tracking
import java.util.Map;     // Added for stock tracking

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane; // Added for error messages
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.MaskFormatter; // Added for Student ID
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class RetailView extends JPanel {
    private GUI gui; // host GUI whose body is where this View is displayed
    private JSplitPane horizontalSplitPane; // divides RetailView with a draggable border into left/right sections

    // Order Display
    private JPanel orderDisplayPanel;
    private DefaultTableModel orderDisplayTableModel;
    private JTable orderDisplayTable;
    private JScrollPane orderDisplayScroll;

    // Order Cost and Confirmation
    private JPanel orderConfirmationPanel;
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JFormattedTextField studentIdField; // Changed to JFormattedTextField
    private JButton resetButton;
    private JButton confirmButton;
    
    // private static final double TAX_RATE = 0.0825; // 8.25% // Replaced by configurable tax rate from Menu

    // Divided panels to show categories, food, modifiers, 
    // and the food item confirmation panel
    private JSplitPane categoryAndOtherSplit;
    private JSplitPane foodAndOtherSplit;
    private JSplitPane modifierAndOtherSplit;
    private JPanel confirmFoodItemPanel;

    // Displays category selection buttons
    private JPanel categoriesPanel;
    private JScrollPane categoryScroll;
    private ButtonGroup categoryButtonGroup;

    // Displays food selection buttons
    private JPanel foodPanel;
    private JScrollPane foodScroll;

    // Displays modifier selection buttons
    private JPanel modifiersPanel;
    private JScrollPane modifiersScroll;

    // Used to select quantity of food item (+ modifier) to add to order
    private SpinnerNumberModel spinnerModel;
    private JSpinner quantitySpinner;
    private JButton confirmFoodItemButton; // Made class member
    private JButton resetFoodItemButton;   // Made class member

    // Tracks the row index of the row currently being filled in for the order
    private int currentRowInOrder;

    private static final int LOW_STOCK_THRESHOLD = 5; // For visual feedback

    public RetailView(GUI gui) {
        this.gui = gui;
        currentRowInOrder = 0;

        setBackground(Color.RED);
        setLayout(new BorderLayout());
        orderDisplayPanel = new JPanel();

        orderDisplayTableModel = new DefaultTableModel(new Object[1][6], new String[] { "Food", "Quantity", "Special",
                "Base Price", "Price with Modifiers", "Total Price", "Remove?" }) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0) {
                    return String.class;
                }
                if (col == 1) {
                    return Integer.class;
                }
                if (col == 2) {
                    return String.class;
                }
                if (col == 3) {
                    return Float.class;
                }
                if (col == 4) {
                    return Float.class;
                }
                if (col == 5) {
                    return Float.class;
                }
                if (col == 6) {
                    return Boolean.class;
                } else {
                    return super.getColumnClass(col);
                }
            }
        };
        orderDisplayTable = new JTable(orderDisplayTableModel);
        // orderDisplayTable.getColumnModel().getColumn(0).setPreferredWidth(25);
        // orderDisplayTable.getColumnModel().getColumn(1).setPreferredWidth(1);
        orderDisplayTable.getColumnModel().getColumn(2).setPreferredWidth(500);
        // orderDisplayTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        orderDisplayTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        // orderDisplayTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        // orderDisplayTable.getColumnModel().getColumn(2).setCellRenderer(new
        // NewLinesCellRenderer());
        orderDisplayTableModel.addTableModelListener(e -> {
            int col = e.getColumn();
            int row = e.getFirstRow();
            if (col == 6) // Check if the event is from the checkbox column
            {
                Boolean isChecked = (boolean) orderDisplayTable.getValueAt(row, col);
                if (isChecked) {
                    orderDisplayTableModel.removeRow(row);
                    currentRowInOrder--;
                    if (orderDisplayTableModel.getRowCount() <= 0) {
                        orderDisplayTableModel.addRow(new Object[orderDisplayTableModel.getColumnCount()]);
                        currentRowInOrder++;
                        resetFoodInput();
                    }
                    updateTotalPriceOfOrder();
                }
            }
        });

        orderDisplayPanel.setLayout(new BorderLayout());
        orderDisplayScroll = new JScrollPane(orderDisplayTable);
        orderDisplayPanel.add(orderDisplayScroll, BorderLayout.CENTER);

        orderConfirmationPanel = new JPanel();
        orderConfirmationPanel.setLayout(new GridBagLayout());

        GridBagConstraints confirmationGBC = new GridBagConstraints();
        confirmationGBC.gridwidth = 2;
        confirmationGBC.gridx = 0;
        confirmationGBC.gridy = 0;
        confirmationGBC.fill = GridBagConstraints.BOTH;
        confirmationGBC.weightx = 1;
        confirmationGBC.weighty = 1;

        subtotalLabel = new JLabel("Subtotal: $0.00");
        subtotalLabel.setFont(new Font(subtotalLabel.getFont().getName(), Font.PLAIN,15));
        orderConfirmationPanel.add(subtotalLabel, confirmationGBC);

        taxLabel = new JLabel("Tax: $0.00");
        taxLabel.setFont(new Font(taxLabel.getFont().getName(), Font.PLAIN, 15));
        confirmationGBC.gridy = 1;
        orderConfirmationPanel.add(taxLabel, confirmationGBC);

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font(totalLabel.getFont().getName(), Font.BOLD, 20));
        confirmationGBC.gridy = 2;
        orderConfirmationPanel.add(totalLabel, confirmationGBC);
        
        try {
            MaskFormatter idMask = new MaskFormatter("######"); // 6 digits
            // idMask.setPlaceholderCharacter('_'); // Optional: if you want a placeholder
            studentIdField = new JFormattedTextField(idMask);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            // Fallback to a simple JTextField if mask fails, though "######" should be safe
            studentIdField = new JFormattedTextField(); 
            System.err.println("Error creating MaskFormatter for Student ID. Using default JFormattedTextField.");
        }
        studentIdField.setColumns(8); // Adjusted columns for 6 digits + border
        studentIdField.setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0), "Student ID (6 digits):"));
        confirmationGBC.gridy = 3;
        orderConfirmationPanel.add(studentIdField, confirmationGBC);

        resetButton = new JButton("Reset Order");
        resetButton.setFocusable(false);
        resetButton.addActionListener(e -> resetOrder());
        confirmationGBC.gridwidth = 1;
        confirmationGBC.gridx = 0;
        confirmationGBC.gridy = 4; // Adjusted gridy
        orderConfirmationPanel.add(resetButton, confirmationGBC);

        confirmButton = new JButton("Confirm Order");
        confirmButton.setFocusable(false);
        confirmButton.addActionListener(e -> confirmOrder());
        confirmationGBC.gridx = 1;
        confirmationGBC.gridy = 4; // Adjusted gridy
        orderConfirmationPanel.add(confirmButton, confirmationGBC);

        orderDisplayPanel.add(orderConfirmationPanel, BorderLayout.SOUTH);

        orderDisplayPanel.setBackground(Color.CYAN);

        categoriesPanel = new JPanel();
        foodPanel = new JPanel();
        modifiersPanel = new JPanel();

        categoryScroll = new JScrollPane(categoriesPanel);
        categoryScroll.getVerticalScrollBar().setUnitIncrement(4);
        categoriesPanel.setBackground(Color.ORANGE);

        foodScroll = new JScrollPane(foodPanel);
        foodScroll.getVerticalScrollBar().setUnitIncrement(4);
        foodPanel.setBackground(Color.RED);

        modifiersScroll = new JScrollPane(modifiersPanel);
        modifiersScroll.getVerticalScrollBar().setUnitIncrement(4);
        modifiersPanel.setBackground(Color.BLACK);
        modifiersPanel.add(new JLabel("HELP"));

        categoriesPanel.setLayout(new GridLayout(0, 3, 5, 5));
        foodPanel.setLayout(new GridLayout(0, 3, 5, 5));
        modifiersPanel.setLayout(new GridLayout(0, 3, 5, 5));

        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        foodPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        modifiersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        confirmFoodItemPanel = new JPanel();
        confirmFoodItemPanel.setLayout(new GridBagLayout());
        confirmFoodItemPanel.setBackground(Color.MAGENTA);
        confirmFoodItemPanel.setVisible(false);

        GridBagConstraints confirmFoodGBC = new GridBagConstraints();
        confirmFoodGBC.gridx = 0;
        confirmFoodGBC.gridy = 0;
        confirmFoodGBC.weightx = 1;
        confirmFoodGBC.weighty = 1;

        modifierAndOtherSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, modifiersScroll, confirmFoodItemPanel);
        modifierAndOtherSplit.setDividerSize(0);
        modifierAndOtherSplit.setResizeWeight(1);
        modifierAndOtherSplit.setOneTouchExpandable(true);
        modifierAndOtherSplit.setVisible(false);

        foodAndOtherSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, foodScroll, modifierAndOtherSplit);
        foodAndOtherSplit.setDividerSize(0);
        foodAndOtherSplit.setResizeWeight(1);
        foodAndOtherSplit.setOneTouchExpandable(true);
        foodAndOtherSplit.setVisible(false);

        categoryAndOtherSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, categoryScroll, foodAndOtherSplit);
        categoryAndOtherSplit.setDividerSize(0);
        categoryAndOtherSplit.setResizeWeight(1);
        categoryAndOtherSplit.setOneTouchExpandable(true);

        horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, orderDisplayPanel, categoryAndOtherSplit);
        horizontalSplitPane.setOneTouchExpandable(true);
        horizontalSplitPane.setResizeWeight(0.6);
        horizontalSplitPane.setDividerSize(10);
        add(horizontalSplitPane);

        renderCategories();
    }

    // Renders category panel and its selectable category input buttons
    public void renderCategories() {
        categoryButtonGroup = new ButtonGroup();
        ArrayList<Category> categoriesList = gui.getPOS().getMenu().getCategories();
        for (int i = 0; i < categoriesList.size(); i++) {
            Category c = categoriesList.get(i);
            RetailButton button = new RetailButton(c + "", c, i, this);
            button.setPreferredSize(new Dimension(100, 100));
            categoryButtonGroup.add(button);
            categoriesPanel.add(button);
        }
    }

    public void renderFoodItems(int categoryIndex) {
        foodPanel.removeAll();
        categoryAndOtherSplit.setDividerSize(10);
        categoryAndOtherSplit.setResizeWeight(0.5);
        categoryAndOtherSplit.resetToPreferredSizes();
        ButtonGroup foodButtonGroup = new ButtonGroup();
        ArrayList<FoodItem> foodItemsList = gui.getPOS().getMenu().getCategories().get(categoryIndex).getFoodItems();
        // Menu menu = gui.getPOS().getMenu(); // Not needed here anymore for stock checking

        for (int i = 0; i < foodItemsList.size(); i++) {
            FoodItem f = foodItemsList.get(i);
            // RetailButton constructor now calls updateStockBasedAppearance()
            RetailButton button = new RetailButton(f + "", f, categoryIndex, i, this);
            button.setPreferredSize(new Dimension(100, 100));
            
            // Removed redundant stock visual feedback logic from here.
            // RetailButton.updateStockBasedAppearance() handles this more comprehensively.

            foodButtonGroup.add(button);
            foodPanel.add(button);
        }
        foodAndOtherSplit.setVisible(true);
        modifierAndOtherSplit.setVisible(false);
        confirmFoodItemPanel.setVisible(false);
        modifiersPanel.removeAll();
        confirmFoodItemPanel.removeAll();
        foodAndOtherSplit.setDividerSize(0);
        modifierAndOtherSplit.setDividerSize(0);
        foodAndOtherSplit.setDividerLocation(0);
        modifierAndOtherSplit.setDividerLocation(0);
    }

    public void renderModifiers(int categoryIndex, int foodItemIndex) {
        ArrayList<Modifier> modifiersList = gui.getPOS().getMenu().getCategories().get(categoryIndex).getFoodItems()
                .get(foodItemIndex).getModifiers();

        modifiersPanel.removeAll();
        foodAndOtherSplit.setDividerSize(10);
        foodAndOtherSplit.setResizeWeight(0.5);
        categoryAndOtherSplit.resetToPreferredSizes();
        foodAndOtherSplit.resetToPreferredSizes();
        // ButtonGroup modifiersButtonGroup = new ButtonGroup();

        JToggleButton noModifiersButton = new JToggleButton("None");
        noModifiersButton.setFocusable(false);
        ArrayList<RetailButton> modifiersButtonList = new ArrayList<RetailButton>();
        noModifiersButton.addActionListener(e -> {
            for (RetailButton modifierButton : modifiersButtonList) {
                modifierButton.setSelected(false);
            }
            orderDisplayTableModel.setValueAt("None", currentRowInOrder, 2);
            orderDisplayTableModel.setValueAt(orderDisplayTableModel.getValueAt(currentRowInOrder, 3),
                    currentRowInOrder, 4);
            NewLinesRowHeightAdjuster.adjustRowHeightForRow(orderDisplayTable, currentRowInOrder);
            renderConfirmFoodItemPanel();
            System.out.println(modifiersPanel.getComponentCount());
        });
        modifiersPanel.add(noModifiersButton);

        modifiersList = gui.getPOS().getMenu().getCategories().get(categoryIndex).getFoodItems().get(foodItemIndex)
                .getModifiers();
        // Menu menuModifiers = gui.getPOS().getMenu(); // Not needed here for stock checking

        for (int i = 0; i < modifiersList.size(); i++) {
            Modifier m = modifiersList.get(i);
            // RetailButton constructor for MODIFIER type doesn't call updateStockBasedAppearance,
            // but its appearance is less critical for stock levels than FoodItem buttons.
            // If modifier stock indication is needed on its button, RetailButton would need adjustment.
            RetailButton button = new RetailButton(m + "", m, categoryIndex, foodItemIndex, i, this);
            button.addActionListener(e -> noModifiersButton.setSelected(false));
            button.setPreferredSize(new Dimension(100, 100));

            // Removed redundant stock visual feedback for Modifier buttons from here.

            modifiersButtonList.add(button);
            modifiersPanel.add(button);
        }
        modifierAndOtherSplit.setVisible(true);
        confirmFoodItemPanel.setVisible(false);
        confirmFoodItemPanel.removeAll();
        modifierAndOtherSplit.setDividerSize(0);
        modifierAndOtherSplit.setDividerLocation(0);
    }

    public void renderConfirmFoodItemPanel() {
        // Initialize components if this is the first time
        if (confirmFoodItemPanel.getComponentCount() == 0) {
            confirmFoodItemButton = new JButton("Confirm Food Item");
            confirmFoodItemButton.addActionListener(e -> addFoodItemToOrder());

            resetFoodItemButton = new JButton("Reset Food Item");
            resetFoodItemButton.addActionListener(e -> clearRow(currentRowInOrder, true));

            spinnerModel = new SpinnerNumberModel(1, 1, 100, 1); // val, min, max, step
            quantitySpinner = new JSpinner(spinnerModel);
            quantitySpinner.setBorder(new TitledBorder(new EmptyBorder(5, 5, 5, 5), "Quantity:"));

            // Simplified ChangeListener
            quantitySpinner.addChangeListener(e -> {
                if (orderDisplayTableModel.getRowCount() > currentRowInOrder && currentRowInOrder >= 0) {
                    Object currentTableValue = orderDisplayTableModel.getValueAt(currentRowInOrder, 1);
                    Object spinnerValue = quantitySpinner.getValue();
                    // Only update table if value is different to prevent potential event loops
                    if (currentTableValue == null || !currentTableValue.equals(spinnerValue)) {
                        orderDisplayTableModel.setValueAt(spinnerValue, currentRowInOrder, 1);
                    }
                }
                updateTotalPriceInRow();
            });
            
            // Layout components
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.insets = new Insets(10, 10, 10, 10);

            gbc.gridx = 0;
            gbc.gridy = 0;
            confirmFoodItemPanel.add(quantitySpinner, gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.weighty = 0;
            gbc.insets = new Insets(10, 0, 10, 0);
            confirmFoodItemPanel.add(confirmFoodItemButton, gbc);

            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.insets = new Insets(10, 10, 10, 10);
            confirmFoodItemPanel.add(resetFoodItemButton, gbc);
        }

        // Logic to update spinner state based on current food item and its stock
        FoodItem currentFoodItem = null;
        if (orderDisplayTableModel.getRowCount() > currentRowInOrder && currentRowInOrder >= 0) {
            Object foodItemObject = orderDisplayTableModel.getValueAt(currentRowInOrder, 0);
            if (foodItemObject instanceof FoodItem) {
                currentFoodItem = (FoodItem) foodItemObject;
            } else if (foodItemObject instanceof String) { // Fallback if only name is stored
                String foodItemName = (String) foodItemObject;
                Menu menu = gui.getPOS().getMenu();
                for (Category cat : menu.getCategories()) {
                    for (FoodItem fi : cat.getFoodItems()) {
                        if (fi.toString().equals(foodItemName)) {
                            currentFoodItem = fi;
                            break;
                        }
                    }
                    if (currentFoodItem != null) break;
                }
            }
        }

        int currentQuantityInTable = 1; // Default to 1 if not set or invalid
        if (currentFoodItem != null && orderDisplayTableModel.getRowCount() > currentRowInOrder && currentRowInOrder >= 0) {
            Object qtyObj = orderDisplayTableModel.getValueAt(currentRowInOrder, 1);
            if (qtyObj instanceof Integer) {
                currentQuantityInTable = (Integer) qtyObj;
            }
        }
        
        if (currentFoodItem != null) {
            Menu menu = gui.getPOS().getMenu();
            
            // Calculate effective total consumption per item considering food item and modifiers
            Map<String, Integer> effectiveRecipeConsumption = new HashMap<>();

            // Add base food item's primary component consumption (if any and tracked)
            if (currentFoodItem.getPrimaryStockComponentId() != null && !currentFoodItem.getPrimaryStockComponentId().isEmpty()) {
                StockableComponent primarySc = menu.getStockableComponentById(currentFoodItem.getPrimaryStockComponentId());
                if (primarySc != null && primarySc.isTrackStock()) {
                     effectiveRecipeConsumption.put(currentFoodItem.getPrimaryStockComponentId(), 
                        effectiveRecipeConsumption.getOrDefault(currentFoodItem.getPrimaryStockComponentId(), 0) + 1);
                } else if (primarySc == null || !primarySc.isTrackStock()) {
                    // If a defined primary component is missing/untracked, effectively 0 can be made if it's essential
                    // This case is tricky; calculateMaxMakeableQuantity handles if food item *alone* is unmakeable.
                    // Here, we are building total demand. If this component is critical and untracked, it's an issue.
                    // For now, assume calculateMaxMakeableQuantity handles the food item's base makeability.
                }
            }

            // Add base food item's recipe consumption
            for (RecipeItem ri : currentFoodItem.getRecipe()) {
                if (ri.getQuantityPerItem() > 0) {
                    effectiveRecipeConsumption.put(ri.getComponentId(), 
                        effectiveRecipeConsumption.getOrDefault(ri.getComponentId(), 0) + ri.getQuantityPerItem());
                }
            }

            // Add consumption from selected modifiers
            ArrayList<Modifier> selectedModifiers = getSelectedModifiers();
            if (selectedModifiers != null && !selectedModifiers.isEmpty()) {
                for (Modifier mod : selectedModifiers) {
                    if (mod.getStockComponentId() != null && !mod.getStockComponentId().isEmpty() && mod.getQuantityEffect() > 0) {
                        effectiveRecipeConsumption.put(mod.getStockComponentId(),
                            effectiveRecipeConsumption.getOrDefault(mod.getStockComponentId(), 0) + mod.getQuantityEffect());
                    }
                }
            }

            int finalMaxMakeable = Integer.MAX_VALUE;
            if (effectiveRecipeConsumption.isEmpty() && (currentFoodItem.getPrimaryStockComponentId() == null || currentFoodItem.getPrimaryStockComponentId().isEmpty())) {
                // No tracked components consumed by food item or its active modifiers, effectively unlimited from this perspective
                finalMaxMakeable = Integer.MAX_VALUE;
            } else if (effectiveRecipeConsumption.isEmpty() && currentFoodItem.getPrimaryStockComponentId() != null && !currentFoodItem.getPrimaryStockComponentId().isEmpty()){
                // Only primary component, no recipe, no consuming modifiers
                StockableComponent primarySc = menu.getStockableComponentById(currentFoodItem.getPrimaryStockComponentId());
                if (primarySc != null && primarySc.isTrackStock()) {
                    finalMaxMakeable = primarySc.getStockQuantity(); // Assumes 1 unit per food item
                } else {
                    finalMaxMakeable = 0; // Untracked/missing essential primary component
                }
            }
            else {
                 boolean possibleToMakeAny = true;
                for (Map.Entry<String, Integer> entry : effectiveRecipeConsumption.entrySet()) {
                    String componentId = entry.getKey();
                    int totalQuantityPerItem = entry.getValue();

                    if (totalQuantityPerItem <= 0) continue; // Not consuming this component

                    StockableComponent sc = menu.getStockableComponentById(componentId);
                    if (sc == null || !sc.isTrackStock()) {
                        finalMaxMakeable = 0; // Required component missing or not tracked
                        possibleToMakeAny = false;
                        break;
                    }
                    finalMaxMakeable = Math.min(finalMaxMakeable, sc.getStockQuantity() / totalQuantityPerItem);
                }
                 if (!possibleToMakeAny) finalMaxMakeable = 0;
            }
            
            // If food item itself is not makeable (e.g. primary component defined but not tracked, and no recipe)
            // this needs to be factored in. calculateMaxMakeableQuantity handles this for the item alone.
            // The above loop calculates based on combined demand. We need the stricter of the two.
            int foodItemBaseMax = currentFoodItem.calculateMaxMakeableQuantity(menu);
            finalMaxMakeable = Math.min(finalMaxMakeable, foodItemBaseMax);


            int spinnerMax = (finalMaxMakeable == Integer.MAX_VALUE) ? 100 : finalMaxMakeable; // Practical limit

            if (finalMaxMakeable == 0) {
                spinnerModel.setMinimum(0);
                spinnerModel.setMaximum(0);
                spinnerModel.setValue(0);
                quantitySpinner.setEnabled(false);
                if (confirmFoodItemButton != null) confirmFoodItemButton.setEnabled(false);
            } else {
                spinnerModel.setMinimum(1);
                spinnerModel.setMaximum(Math.max(1, spinnerMax)); // Ensure max is at least 1
                
                int newSpinnerValue = Math.max((Integer)spinnerModel.getMinimum(), Math.min(currentQuantityInTable, (Integer)spinnerModel.getMaximum()));
                if (currentQuantityInTable == 0 && newSpinnerValue == 0 && (Integer)spinnerModel.getMinimum() == 1 && finalMaxMakeable > 0) {
                     newSpinnerValue = 1; // If item was out of stock (qty 0) and now is makeable, set to 1
                }
                spinnerModel.setValue(newSpinnerValue);
                
                quantitySpinner.setEnabled(true);
                if (confirmFoodItemButton != null) confirmFoodItemButton.setEnabled(true);
            }
        } else { // No food item selected, or item not found
            spinnerModel.setMinimum(0);
            spinnerModel.setMaximum(0);
            spinnerModel.setValue(0);
            quantitySpinner.setEnabled(false);
            if (confirmFoodItemButton != null) confirmFoodItemButton.setEnabled(false);
        }

        // Ensure table reflects the spinner's (potentially corrected) value
        if (orderDisplayTableModel.getRowCount() > currentRowInOrder && currentRowInOrder >= 0) {
            Object currentTableValue = orderDisplayTableModel.getValueAt(currentRowInOrder, 1);
            if (currentTableValue == null || !currentTableValue.equals(spinnerModel.getValue())) {
                 orderDisplayTableModel.setValueAt(spinnerModel.getValue(), currentRowInOrder, 1);
            }
        }
        updateTotalPriceInRow(); // Update price based on new quantity

        modifierAndOtherSplit.setDividerSize(10);
        modifierAndOtherSplit.setResizeWeight(0.5);
        categoryAndOtherSplit.resetToPreferredSizes();
        foodAndOtherSplit.resetToPreferredSizes();
        modifierAndOtherSplit.resetToPreferredSizes();
        confirmFoodItemPanel.setVisible(true);
    }

    public GUI getGUI() {
        return gui;
    }

    public TableModel getOrderDisplayTableModel() {
        return orderDisplayTableModel;
    }

    public JTable getOrderDisplayTable() {
        return orderDisplayTable;
    }

    public int getCurrentRowInOrder() {
        return currentRowInOrder;
    }

    // returns a list of modifiers whose buttons are selected
    public ArrayList<Modifier> getSelectedModifiers() {
        ArrayList<Modifier> selectedModifiers = new ArrayList<Modifier>();
        for (Component c : modifiersPanel.getComponents())
            if (c instanceof RetailButton) {
                RetailButton modifierButton = (RetailButton) c;
                Modifier modifier = modifierButton.getModifier();
                if (modifierButton.isSelected()) {
                    selectedModifiers.add(modifier);
                }
            }
        return selectedModifiers;
    }

    public void addFoodItemToOrder() {
        // Creates a new row that can be filled with a new food entry
        orderDisplayTableModel.addRow(new Object[orderDisplayTableModel.getColumnCount()]);
        currentRowInOrder++;
        updateTotalPriceOfOrder();
        resetFoodInput();
    }

    public void clearRow(int row, boolean resetFoodInput) {
        for (int column = 0; column < orderDisplayTable.getColumnCount() - 1; column++) {
            orderDisplayTable.setValueAt(null, row, column);
        }
        orderDisplayTable.setValueAt(false, row, 6);
        if (resetFoodInput == true)
            resetFoodInput();
    }

    public void resetFoodInput() {
        foodPanel.removeAll();
        modifiersPanel.removeAll();
        confirmFoodItemPanel.removeAll();
        categoryAndOtherSplit.setDividerSize(0);
        foodAndOtherSplit.setDividerSize(0);
        foodAndOtherSplit.setVisible(false);
        modifierAndOtherSplit.setDividerSize(0);
        modifierAndOtherSplit.setVisible(false);
        confirmFoodItemPanel.setVisible(false);
        categoryAndOtherSplit.setDividerLocation(0);
        categoryButtonGroup.clearSelection();
    }

    public void resetOrder() {
        int initialRowCount = orderDisplayTable.getRowCount();
        resetFoodInput();
        for (int row = 0; row < initialRowCount; row++) {
            orderDisplayTableModel.removeRow(0);
        }
        currentRowInOrder = 0;
        orderDisplayTableModel.addRow(new Object[orderDisplayTableModel.getColumnCount()]);
        studentIdField.setValue(null); // Clear student ID field for JFormattedTextField
        updateTotalPriceOfOrder();
    }

    public void confirmOrder() {
        if (orderDisplayTable.getRowCount() > 1) {
            // Attempt to get the currently selected category's index BEFORE resetting.
            // This is tricky because categoryButtonGroup.getSelection() gives a ButtonModel, not the RetailButton directly.
            // A simpler approach for now is to rely on the user re-selecting, or enhance RetailButton/category selection tracking later.
            // For now, the existing resetOrder() will clear panels. The user must re-click a category to see updated stock colors.
            // The core issue is that RetailView doesn't "remember" the last selected category index easily after resetFoodInput clears selections.

            saveOrder(); // This now handles stock decrement and saves the menu
            resetOrder(); // This calls resetFoodInput, which clears the item panels

            // To see updated stock colors on buttons, the user currently needs to re-select a category.
            // The renderFoodItems and renderModifiers methods contain the logic to set button colors.
            // These methods are triggered by user interaction (clicking a category/food item button).
            // The stock values themselves ARE updated by saveOrder().
        }
    }

    // Saves a singular new order to the database
    public void saveOrder() {
        Menu menu = gui.getPOS().getMenu();
        Map<String, Integer> requiredComponents = new HashMap<>();
        List<String> outOfStockMessages = new ArrayList<>();

        // --- Phase 1: Pre-check stock availability ---
        for (int row = 0; row < orderDisplayTable.getRowCount() - 1; row++) {
            if (orderDisplayTable.getValueAt(row, 0) == null) continue; // Skip empty/partially filled rows

            String foodItemName = orderDisplayTable.getValueAt(row, 0).toString();
            int itemQuantity = Integer.parseInt(orderDisplayTable.getValueAt(row, 1).toString());
            String specialRequestString = orderDisplayTable.getValueAt(row, 2) != null ? orderDisplayTable.getValueAt(row, 2).toString() : "";

            FoodItem currentFoodItem = null;
            Category parentCategory = null; // To help find modifiers if needed, though modifiers are on FoodItem

            // Find FoodItem
            for (Category cat : menu.getCategories()) {
                for (FoodItem fi : cat.getFoodItems()) {
                    if (fi.toString().equals(foodItemName)) {
                        currentFoodItem = fi;
                        parentCategory = cat;
                        break;
                    }
                }
                if (currentFoodItem != null) break;
            }

            if (currentFoodItem == null) {
                System.err.println("RetailView.saveOrder: Could not find FoodItem '" + foodItemName + "' in menu during stock check. Skipping stock for this item.");
                continue; 
            }

            // Check primary component
            if (currentFoodItem.getPrimaryStockComponentId() != null) {
                requiredComponents.put(currentFoodItem.getPrimaryStockComponentId(),
                        requiredComponents.getOrDefault(currentFoodItem.getPrimaryStockComponentId(), 0) + itemQuantity);
            }

            // Check recipe components
            for (RecipeItem recipeItem : currentFoodItem.getRecipe()) {
                requiredComponents.put(recipeItem.getComponentId(),
                        requiredComponents.getOrDefault(recipeItem.getComponentId(), 0) + (recipeItem.getQuantityPerItem() * itemQuantity));
            }

            // Check modifiers
            if (!specialRequestString.isEmpty() && !specialRequestString.equalsIgnoreCase("None")) {
                String[] modifierNames = specialRequestString.split(",\\s*"); // Split by comma and optional space
                for (String modName : modifierNames) {
                    Modifier currentModifier = null;
                    for (Modifier mod : currentFoodItem.getModifiers()) {
                        if (mod.toString().equals(modName.trim())) {
                            currentModifier = mod;
                            break;
                        }
                    }
                    if (currentModifier != null && currentModifier.getStockComponentId() != null) {
                        requiredComponents.put(currentModifier.getStockComponentId(),
                                requiredComponents.getOrDefault(currentModifier.getStockComponentId(), 0) + itemQuantity);
                    }
                }
            }
        }

        // Verify stock levels
        for (Map.Entry<String, Integer> entry : requiredComponents.entrySet()) {
            String componentId = entry.getKey();
            int neededQuantity = entry.getValue();
            StockableComponent sc = menu.getStockableComponentById(componentId);

            if (sc == null) {
                // This case should ideally not happen if menu data is consistent
                outOfStockMessages.add("Component ID " + componentId + " not found in inventory.");
                continue;
            }
            if (sc.isTrackStock() && sc.getStockQuantity() < neededQuantity) {
                outOfStockMessages.add(sc.getName() + " - Required: " + neededQuantity + ", Available: " + sc.getStockQuantity());
            }
        }

        if (!outOfStockMessages.isEmpty()) {
            StringBuilder message = new StringBuilder("Cannot complete order. The following items are out of stock or have insufficient quantity:\n");
            for (String msg : outOfStockMessages) {
                message.append("- ").append(msg).append("\n");
            }
            JOptionPane.showMessageDialog(this, message.toString(), "Out of Stock", JOptionPane.ERROR_MESSAGE);
            return; // Abort order
        }

        // --- Phase 2: If stock is available, proceed to create order and decrement stock ---
        Order order = new Order();
        Object[] orderRow = new Object[8]; // Updated to 8 columns

        for (int row = 0; row < orderDisplayTable.getRowCount() - 1; row++) {
             if (orderDisplayTable.getValueAt(row, 0) == null) continue;

            String foodItemName = orderDisplayTable.getValueAt(row, 0).toString();
            int itemQuantity = Integer.parseInt(orderDisplayTable.getValueAt(row, 1).toString());
            String specialRequest = orderDisplayTable.getValueAt(row, 2).toString().replaceAll(", ", "; ");
            order.addOrderItem(foodItemName);
            order.addItemQuant(itemQuantity);
            order.addSpecialRequest(specialRequest);

            // Decrement stock (find FoodItem and Modifiers again - could optimize by storing them from phase 1)
            FoodItem currentFoodItem = null;
             for (Category cat : menu.getCategories()) {
                for (FoodItem fi : cat.getFoodItems()) {
                    if (fi.toString().equals(foodItemName)) {
                        currentFoodItem = fi;
                        break;
                    }
                }
                if (currentFoodItem != null) break;
            }

            if (currentFoodItem != null) {
                // Decrement primary component
                if (currentFoodItem.getPrimaryStockComponentId() != null) {
                    StockableComponent scPrimary = menu.getStockableComponentById(currentFoodItem.getPrimaryStockComponentId());
                    if (scPrimary != null && scPrimary.isTrackStock()) {
                        scPrimary.decrementStock(itemQuantity);
                        menu.updateStockableComponent(scPrimary); // Important to update in Menu's list
                    }
                }
                // Decrement recipe components
                for (RecipeItem recipeItem : currentFoodItem.getRecipe()) {
                    StockableComponent scRecipe = menu.getStockableComponentById(recipeItem.getComponentId());
                    if (scRecipe != null && scRecipe.isTrackStock()) {
                        scRecipe.decrementStock(recipeItem.getQuantityPerItem() * itemQuantity);
                        menu.updateStockableComponent(scRecipe);
                    }
                }
                // Decrement modifiers
                if (!specialRequest.isEmpty() && !specialRequest.equalsIgnoreCase("None")) {
                    String[] modifierNames = specialRequest.split(";\\s*"); // Using semicolon as per order.addSpecialRequest
                     for (String modName : modifierNames) {
                        Modifier currentModifier = null;
                        for (Modifier mod : currentFoodItem.getModifiers()) {
                            if (mod.toString().equals(modName.trim())) {
                                currentModifier = mod;
                                break;
                            }
                        }
                        if (currentModifier != null && currentModifier.getStockComponentId() != null) {
                            StockableComponent scMod = menu.getStockableComponentById(currentModifier.getStockComponentId());
                            if (scMod != null && scMod.isTrackStock()) {
                                scMod.decrementStock(itemQuantity);
                                menu.updateStockableComponent(scMod);
                            }
                        }
                    }
                }
            }
        }
        
        menu.saveMenu(); // Persist stock changes

        order.sortFoodByAlphabet();
        String studentIdString = (String) studentIdField.getValue();
        if (studentIdString != null) {
            studentIdString = studentIdString.trim(); 
        }
        order.setStudentID(studentIdString != null ? studentIdString : "");

        orderRow[0] = order.getFood().toString().replaceAll("[\\[\\]]", "").replaceAll(", ", "\n");
        orderRow[1] = order.getQuantity().toString().replaceAll("[\\[\\]]", "").replaceAll(", ", "\n");
        orderRow[2] = order.getSpecial().toString().replaceAll("[\\[\\]]", "").replaceAll(", ", "\n").replaceAll("; ",
                ", ");
        orderRow[3] = order.getStatus();
        orderRow[4] = order.getDateTime().format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a"));
        orderRow[5] = order.getCompletionDate() != null ? order.getCompletionDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm:ss a")) : "";
        orderRow[6] = order.getStudentID();
        orderRow[7] = order.getID();

        gui.getPOS().addOrder(order);
        gui.getPOS().getOrdersTableModel().addRow(OrdersPreparer.prepareOrder(order));

        JTable currentOrdersTable = gui.getKitchenView().getCurrentOrdersTable();
        JTable orderHistoryTable = gui.getMgmtView().getOrderHistoryTable();

        for (int row = 0; row < currentOrdersTable.getRowCount(); row++) {
            NewLinesRowHeightAdjuster.adjustRowHeightForRow(currentOrdersTable, row);
        }
        for (int row = 0; row < orderHistoryTable.getRowCount(); row++) {
            NewLinesRowHeightAdjuster.adjustRowHeightForRow(orderHistoryTable, row);
        }

        gui.getPOS().saveOrders(); // This should save the order data itself. Menu (with stock) was saved above.
        gui.getPOS().sendData(order);
    }

    public void saveOrderWithoutSending(Order order) {
        order.sortFoodByAlphabet();
        // Use the OrdersPreparer to create the row for the table model
        Object[] orderRow = OrdersPreparer.prepareOrder(order);

        gui.getPOS().addOrder(order);
        gui.getPOS().getOrdersTableModel().addRow(orderRow);

        JTable currentOrdersTable = gui.getKitchenView().getCurrentOrdersTable();
        JTable orderHistoryTable = gui.getMgmtView().getOrderHistoryTable();

        for (int row = 0; row < currentOrdersTable.getRowCount(); row++) {
            NewLinesRowHeightAdjuster.adjustRowHeightForRow(currentOrdersTable, row);
        }
        for (int row = 0; row < orderHistoryTable.getRowCount(); row++) {
            NewLinesRowHeightAdjuster.adjustRowHeightForRow(orderHistoryTable, row);
        }

        gui.getPOS().saveOrders();
    }

    // Calculates total price of a food item (row) including modifiers and quantity
    // Price = (basePrice + (Sum of Modifiers)) * quantity
    public void updateTotalPriceInRow() {
        orderDisplayTable.setValueAt(
                (Integer) quantitySpinner.getValue() * (Double) orderDisplayTable.getValueAt(currentRowInOrder, 4),
                currentRowInOrder, 5);
    }

    // Calculates subtotal price of order (sum of food item totals)   
    public double calculateSubtotal() {
        double subtotal = 0;
        for (int row = 0; row < orderDisplayTable.getRowCount() - 1; row++) {
            if (orderDisplayTable.getValueAt(row, 5) != null) { // Check for null before casting
                double totalPriceOfRow = (double) orderDisplayTable.getValueAt(row, 5);
                subtotal += totalPriceOfRow;
            }
        }
        return subtotal;
    }

    // Calculates tax based on subtotal
    public double calculateTax(double subtotal) {
        return subtotal * gui.getPOS().getMenu().getTaxRate();
    }

    // Calculates total price including tax
    public double calculateTotal(double subtotal, double tax) {
        return subtotal + tax;
    }

    // Displays the new subtotal, tax, and total price of order
    public void updateTotalPriceOfOrder() {
        double subtotal = calculateSubtotal();
        double tax = calculateTax(subtotal);
        double total = calculateTotal(subtotal, tax);

        subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
        taxLabel.setText(String.format("Tax: $%.2f", tax));
        totalLabel.setText(String.format("Total: $%.2f", total));
    }
}
