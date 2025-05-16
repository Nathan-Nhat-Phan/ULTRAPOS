
/** 
 * MenuCreator.java
 * Panel responsible for menu navigation/creation/editing
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.JComboBox; // Added for dropdowns
import javax.swing.JTabbedPane; // Added for tabbed interface
import javax.swing.JCheckBox; // Added for boolean table cells
import javax.swing.SpinnerNumberModel; // Added for number input
import javax.swing.JSpinner; // Added for number input
import javax.swing.text.NumberFormatter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.FlowLayout;
import javax.swing.table.DefaultTableModel;

public class MenuCreator extends JPanel {
    // Dividing panel into sections for each menu entry type
    private JSplitPane categoryAndOtherSplit;
    private JSplitPane foodAndOtherSplit; 

    private GUI gui; // holds MgmtView that MenuCreator resides in; manages GUI components
    private Menu menu; // embodiment of menu data

    // Display components specific to menu entry types
    private JPanel categoriesPanel;
    private JPanel foodPanel;
    private JPanel modifiersPanel;

    // Display menu entries
    private JTable categoriesTable;
    private JTable foodTable;
    private JTable modifiersTable;

    // Embodies menu entry data for JTable use
    private DefaultTableModel categoriesTableModel;
    private DefaultTableModel foodTableModel;
    private DefaultTableModel modifiersTableModel;

    // Scrollability for tables
    private JScrollPane categoriesScroll;
    private JScrollPane foodScroll;
    private JScrollPane modifiersScroll;
    private JScrollPane stockableComponentsScroll; // For the new table

    private JToggleButton openFoodButton; // When toggled, displays selected Category's FoodItems
    private JToggleButton openModifiersButton; // When toggled, displays selected FoodItem's Modifiers

    private JFormattedTextField taxRateField;
    private JButton saveTaxRateButton;

    // For the new Inventory Components tab
    private JTabbedPane mainTabbedPane;
    private JPanel stockableComponentsPanel;
    private JTable stockableComponentsTable;
    private DefaultTableModel stockableComponentsTableModel;


    public MenuCreator(GUI gui) {
        this.gui = gui;
        this.menu = gui.getPOS().getMenu();
        setPreferredSize(new Dimension(1000, 550)); // Increased preferred height for new panel and tabs

        // Main panel uses BorderLayout
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F0F0F0")); // Light gray background for the creator panel

        // --- Tax Rate Settings Panel ---
        JPanel taxSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        taxSettingsPanel.setBorder(BorderFactory.createTitledBorder("Global Menu Settings"));
        
        JLabel taxRateLabel = new JLabel("Current Tax Rate (e.g., 0.0825 for 8.25%):");
        
        NumberFormat percentFormat = new DecimalFormat("0.0#####"); // Allows for several decimal places
        NumberFormatter taxFormatter = new NumberFormatter(percentFormat);
        taxFormatter.setValueClass(Double.class);
        taxFormatter.setMinimum(0.0);
        taxFormatter.setMaximum(1.0); // Tax rate as a decimal (0 to 1)
        taxFormatter.setAllowsInvalid(false);
        taxFormatter.setCommitsOnValidEdit(true);

        taxRateField = new JFormattedTextField(taxFormatter);
        taxRateField.setValue(menu.getTaxRate()); // Initialize with current tax rate
        taxRateField.setColumns(8); // Set preferred size

        saveTaxRateButton = new JButton("Save Tax Rate");
        saveTaxRateButton.addActionListener(e -> saveTaxRate());

        taxSettingsPanel.add(taxRateLabel);
        taxSettingsPanel.add(taxRateField);
        taxSettingsPanel.add(saveTaxRateButton);
        
        add(taxSettingsPanel, BorderLayout.NORTH);

        // --- Categories, Food Items, Modifiers Panels ---
        categoriesPanel = new JPanel();
        foodPanel = new JPanel();
        modifiersPanel = new JPanel();

        categoriesPanel.setLayout(new BorderLayout());
        foodPanel.setLayout(new BorderLayout());
        modifiersPanel.setLayout(new BorderLayout());

        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        foodPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        modifiersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        foodAndOtherSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, foodPanel, modifiersPanel);
        foodAndOtherSplit.setDividerSize(0);
        foodAndOtherSplit.setResizeWeight(1);
        foodAndOtherSplit.setOneTouchExpandable(true);
        foodAndOtherSplit.setVisible(false);

        categoryAndOtherSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, categoriesPanel, foodAndOtherSplit);
        categoryAndOtherSplit.setDividerSize(10); // Make divider visible
        categoryAndOtherSplit.setResizeWeight(0.3); // Give categories panel a decent initial size
        categoryAndOtherSplit.setOneTouchExpandable(true);

        // Create a panel to hold the existing category/food/modifier structure
        JPanel menuItemsPanel = new JPanel(new BorderLayout());
        menuItemsPanel.add(categoryAndOtherSplit, BorderLayout.CENTER);

        // --- Stockable Components Panel (New Tab) ---
        stockableComponentsPanel = new JPanel(new BorderLayout());
        stockableComponentsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        initializeInventoryComponentsPanel(); // Setup this new panel

        // --- Main Tabbed Pane ---
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Menu Items", menuItemsPanel);
        mainTabbedPane.addTab("Inventory Components", stockableComponentsPanel);

        mainTabbedPane.addChangeListener(e -> {
            if (mainTabbedPane.getSelectedComponent() == stockableComponentsPanel) {
                refreshStockableComponentsTable();
            }
            // Potentially refresh other tabs if needed when they are selected
        });

        add(mainTabbedPane, BorderLayout.CENTER); // Add tabbed pane to the center

        renderCategories(); // Initial render for the first tab
        
        // Refresh tax rate field if menu is reloaded or POS instance changes (if applicable)
        // This might be needed if the Menu object can be swapped out.
        // For now, it's initialized at construction.
    }

    private void saveTaxRate() {
        try {
            double newRate = ((Number) taxRateField.getValue()).doubleValue();
            if (newRate >= 0.0 && newRate <= 1.0) {
                menu.setTaxRate(newRate);
                menu.saveMenu();
                JOptionPane.showMessageDialog(null, "Tax rate saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid tax rate. Please enter a value between 0.0 and 1.0.", "Error", JOptionPane.ERROR_MESSAGE);
                taxRateField.setValue(menu.getTaxRate()); // Reset to current saved rate
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Invalid tax rate format. Please enter a valid number (e.g., 0.0825).", "Error", JOptionPane.ERROR_MESSAGE);
            taxRateField.setValue(menu.getTaxRate()); // Reset to current saved rate
        }
    }

    // Renders panel to View and Edit Categories selection
    public void renderCategories() {
        modifiersPanel.removeAll();

        ArrayList<Category> categoriesList = gui.getPOS().getMenu().getCategories();
        createCategoriesTable(categoriesList);

        categoriesScroll = new JScrollPane(categoriesTable);
        categoriesScroll.setPreferredSize(new Dimension(0, 0));
        categoriesTable.setFillsViewportHeight(true);
        categoriesTable.setPreferredScrollableViewportSize(new Dimension(10, 10));
        categoriesPanel.add(categoriesScroll, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new GridBagLayout());

        openFoodButton = new JToggleButton("Open Food Items");
        openFoodButton.addActionListener(e -> {
            if (openFoodButton.isSelected() && categoriesTable.getSelectedRow() != -1) {
                openFoodButton.setText("Close Food Items");
                renderFoodItems(getSelectedCategory());
            } else {
                detoggleOpenFoodButton();
                closeFoodItems();
            }
        });

        JButton modifyButton = new JButton("Modify Selected Category");
        modifyButton.addActionListener(e -> {
            if (categoriesTable.getSelectedRow() != -1) {
                modifyCategory(getSelectedCategory());
            }
        });
        JButton addButton = new JButton("Add New Category");
        addButton.addActionListener(e -> addCategory());
        JButton removeButton = new JButton("Remove Selected Category");
        removeButton.addActionListener(e -> removeCategory(categoriesTable.getSelectedRow()));

        categoriesTable.getSelectionModel().addListSelectionListener(e -> {
            if (categoriesTable.getSelectedRow() != -1 && openFoodButton.isSelected()) {
                renderFoodItems(getSelectedCategory());
            }
        });

        GridBagConstraints buttonsGBC = new GridBagConstraints();
        buttonsGBC.fill = GridBagConstraints.BOTH;
        buttonsGBC.gridx = 0;
        buttonsGBC.gridy = 0;
        buttonsGBC.gridwidth = 2;
        buttonsPanel.add(modifyButton, buttonsGBC);
        buttonsGBC.gridwidth = 1;

        buttonsGBC.gridx = 0;
        buttonsGBC.gridy = 1;
        buttonsPanel.add(addButton, buttonsGBC);
        buttonsGBC.gridx = 1;
        buttonsGBC.gridy = 1;
        buttonsPanel.add(removeButton, buttonsGBC);
        buttonsGBC.gridx = 0;
        buttonsGBC.gridy = 2;
        buttonsGBC.gridwidth = 2;
        buttonsPanel.add(openFoodButton, buttonsGBC);
        /*
         * buttonsGBC.gridx = 1;
         * buttonsGBC.gridy = 2;
         * buttonsPanel.add(closeButton, buttonsGBC);
         */

        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        categoriesPanel.add(buttonsPanel, BorderLayout.EAST);
    }

    public void renderFoodItems(Category category) {
        foodPanel.removeAll();
        modifiersPanel.removeAll();
        modifiersPanel.setVisible(false);
        foodAndOtherSplit.setVisible(true);
        categoryAndOtherSplit.setDividerSize(10);
        categoryAndOtherSplit.setResizeWeight(0.5);
        categoryAndOtherSplit.resetToPreferredSizes();
        foodAndOtherSplit.setDividerLocation(0);

        ArrayList<FoodItem> foodItemsList = category.getFoodItems();
        createFoodItemsTable(foodItemsList);
        foodScroll = new JScrollPane(foodTable);
        foodTable.setPreferredScrollableViewportSize(new Dimension(10, 10));
        foodTable.setFillsViewportHeight(true);
        foodPanel.add(foodScroll, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new GridBagLayout());

        JButton modifyButton = new JButton("Modify Selected Food Item");
        modifyButton.addActionListener(e -> {
            if (foodTable.getSelectedRow() != -1) {
                modifyFoodItem(getSelectedFoodItem());
            }
        });

        openModifiersButton = new JToggleButton("Open Modifiers");
        openModifiersButton.addActionListener(e -> {
            if (openModifiersButton.isSelected() && foodTable.getSelectedRow() != -1) {
                openModifiersButton.setText("Close Modifiers");
                renderModifiers(getSelectedFoodItem());
            } else {
                detoggleOpenModifiersButton();
                closeModifiers();
            }
        });

        JButton addButton = new JButton("Add New Food Item");
        addButton.addActionListener(e -> addFoodItem());

        JButton removeButton = new JButton("Remove Selected Food Item");
        removeButton.addActionListener(e -> {
            removeFoodItem(foodTable.getSelectedRow());
        });

        foodTable.getSelectionModel().addListSelectionListener(e -> {
            if (foodTable.getSelectedRow() != -1 && openModifiersButton.isSelected()) {
                renderModifiers(getSelectedFoodItem());
            }
        });

        GridBagConstraints buttonsGBC = new GridBagConstraints();
        buttonsGBC.fill = GridBagConstraints.BOTH;
        buttonsGBC.gridx = 0;
        buttonsGBC.gridy = 0;
        buttonsGBC.gridwidth = 2;
        buttonsPanel.add(modifyButton, buttonsGBC);
        buttonsGBC.gridwidth = 1;

        buttonsGBC.gridx = 0;
        buttonsGBC.gridy = 1;
        buttonsPanel.add(addButton, buttonsGBC);
        buttonsGBC.gridx = 1;
        buttonsGBC.gridy = 1;
        buttonsPanel.add(removeButton, buttonsGBC);
        buttonsGBC.gridx = 0;
        buttonsGBC.gridy = 2;
        buttonsGBC.gridwidth = 2;
        buttonsPanel.add(openModifiersButton, buttonsGBC);

        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        foodPanel.add(buttonsPanel, BorderLayout.EAST);
    }

    public void renderModifiers(FoodItem foodItem) {
        modifiersPanel.removeAll();
        modifiersPanel.setVisible(true);
        foodAndOtherSplit.setDividerSize(10);
        foodAndOtherSplit.setResizeWeight(0.5);
        categoryAndOtherSplit.resetToPreferredSizes();
        foodAndOtherSplit.resetToPreferredSizes();

        ArrayList<Modifier> modifiersList = foodItem.getModifiers();
        createModifiersTable(modifiersList);
        modifiersScroll = new JScrollPane(modifiersTable);
        modifiersTable.setPreferredScrollableViewportSize(new Dimension(10, 10));
        modifiersTable.setFillsViewportHeight(true);
        modifiersPanel.add(modifiersScroll, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new GridBagLayout());

        JButton modifyButton = new JButton("Modify Selected Modifier");
        modifyButton.addActionListener(e -> {
            if (modifiersTable.getSelectedRow() != -1) {
                modifyModifier(getSelectedModifier());
            }
        });

        JButton addButton = new JButton("Add New Modifier");
        addButton.addActionListener(e -> addModifier());

        JButton removeButton = new JButton("Remove Selected Modifier");
        removeButton.addActionListener(e -> removeModifier(modifiersTable.getSelectedRow()));

        GridBagConstraints buttonsGBC = new GridBagConstraints();
        buttonsGBC.fill = GridBagConstraints.BOTH;
        buttonsGBC.gridx = 0;
        buttonsGBC.gridy = 0;
        buttonsGBC.gridwidth = 2;
        buttonsPanel.add(modifyButton, buttonsGBC);
        buttonsGBC.gridwidth = 1;

        buttonsGBC.gridx = 0;
        buttonsGBC.gridy = 1;
        buttonsPanel.add(addButton, buttonsGBC);
        buttonsGBC.gridx = 1;
        buttonsGBC.gridy = 1;
        buttonsPanel.add(removeButton, buttonsGBC);

        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        modifiersPanel.add(buttonsPanel, BorderLayout.EAST);
    }

    public void createCategoriesTable(ArrayList<Category> categoriesList) {
        /*
         * Object[] columnNames = {"Category", "Default Modifiers"};
         * Object[][] categoryData = new Object[categoriesList.size()][2];
         */

        Object[] columnNames = { "Category", "ID" };
        Object[][] categoryData = new Object[categoriesList.size()][2];
        for (int i = 0; i < categoriesList.size(); i++) {
            Category category = categoriesList.get(i);
            Object[] categoryRow = new Object[2];
            categoryRow[0] = category;
            categoryRow[1] = category.getID();
            /*
             * if (category.getDefaultModifiers() != null)
             * {
             * categoryRow[1]=
             * category.getDefaultModifiers().toString().replaceAll("[\\[\\]]", "");
             * }
             */
            categoryData[i] = categoryRow;
        }
        categoriesTableModel = new DefaultTableModel(categoryData, columnNames);
        categoriesTable = new JTable(categoriesTableModel);
        categoriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // categoriesTable.getColumnModel().getColumn(1).setPreferredWidth(800);
    }

    public void createFoodItemsTable(ArrayList<FoodItem> foodItemsList) {
        Object[] columnNames = { "Food Item", "Base Cost", "ID" };
        Object[][] foodData = new Object[foodItemsList.size()][3];
        for (int i = 0; i < foodItemsList.size(); i++) {
            FoodItem foodItem = foodItemsList.get(i);
            Object[] foodRow = new Object[3];
            foodRow[0] = foodItem;
            foodRow[1] = foodItem.getPrice();
            foodRow[2] = foodItem.getID();
            foodData[i] = foodRow;
        }
        foodTableModel = new DefaultTableModel(foodData, columnNames);
        foodTable = new JTable(foodTableModel);
        foodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void createModifiersTable(ArrayList<Modifier> modifiersList) {
        Object[] columnNames = { "Modifier", "Added Cost", "ID" }; // Existing columns
        // Potentially add "Stock Component" column later if needed for direct display
        Object[][] modifierData = new Object[modifiersList.size()][3];
        for (int i = 0; i < modifiersList.size(); i++) {
            Modifier modifier = modifiersList.get(i);
            Object[] modifierRow = new Object[3];
            modifierRow[0] = modifier;
            modifierRow[1] = modifier.getAddCost();
            modifierRow[2] = modifier.getID();
            modifierData[i] = modifierRow;
        }
        modifiersTableModel = new DefaultTableModel(modifierData, columnNames);
        modifiersTable = new JTable(modifiersTableModel);
        modifiersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public GUI getGUI() {
        return gui;
    }

    public void closeFoodItems() {
        closeModifiers();
        foodPanel.removeAll();
        modifiersPanel.removeAll();
        categoryAndOtherSplit.setDividerSize(0);
        foodAndOtherSplit.setDividerSize(0);
        foodAndOtherSplit.setVisible(false);
        categoryAndOtherSplit.setDividerLocation(0);
    }

    public void closeModifiers() {
        modifiersPanel.removeAll();
        modifiersPanel.setVisible(false);
        foodAndOtherSplit.setDividerSize(0);
        foodAndOtherSplit.setDividerLocation(0);
    }

    public void modifyCategory(Category category) {
        JPanel customMessage = new JPanel(new GridLayout(0, 1));
        JTextField nameField = new JTextField(25);
        nameField.setText(category.toString());

        customMessage.add(new JLabel("What is the new name of this category?"));
        customMessage.add(nameField);

        String nameText = "";
        boolean keepAsking = true;
        while (keepAsking) {
            int result = JOptionPane.showConfirmDialog(null, customMessage, "Modify Category",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            nameText = nameField.getText();
            if (result == JOptionPane.OK_OPTION) {
                // Validate the name input
                if (nameText.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid name.");
                    continue;
                }
                keepAsking = false;
            } else {
                return;
            }
        }
        category.setName(nameText);
        categoriesTableModel.setValueAt(category, categoriesTable.getSelectedRow(), 0);
        categoriesTableModel.setValueAt(category.getID(), categoriesTable.getSelectedRow(), 1);
        menu.saveMenu();
        menu.sendData(category);
    }

    public void modifyFoodItem(FoodItem foodItem) {
        // --- Basic Info ---
        JPanel basicInfoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcBasic = new GridBagConstraints();
        gbcBasic.insets = new java.awt.Insets(2,2,2,2);
        gbcBasic.anchor = GridBagConstraints.WEST;

        gbcBasic.gridx = 0; gbcBasic.gridy = 0;
        basicInfoPanel.add(new JLabel("Food Item Name:"), gbcBasic);
        JTextField nameField = new JTextField(foodItem.toString(), 25);
        gbcBasic.gridx = 1; gbcBasic.gridy = 0; gbcBasic.fill = GridBagConstraints.HORIZONTAL; gbcBasic.weightx = 1;
        basicInfoPanel.add(nameField, gbcBasic);

        gbcBasic.gridx = 0; gbcBasic.gridy = 1; gbcBasic.fill = GridBagConstraints.NONE; gbcBasic.weightx = 0;
        basicInfoPanel.add(new JLabel("Base Cost:"), gbcBasic);
        JTextField costField = new JTextField(((Double) foodItem.getPrice()).toString(), 10);
        gbcBasic.gridx = 1; gbcBasic.gridy = 1; gbcBasic.fill = GridBagConstraints.HORIZONTAL; gbcBasic.weightx = 1;
        basicInfoPanel.add(costField, gbcBasic);

        // --- Primary Stock Component ---
        gbcBasic.gridx = 0; gbcBasic.gridy = 2; gbcBasic.fill = GridBagConstraints.NONE; gbcBasic.weightx = 0;
        basicInfoPanel.add(new JLabel("Primary Stock Component:"), gbcBasic);
        ArrayList<StockableComponent> allComponents = menu.getAllStockableComponents();
        JComboBox<StockableComponentWrapper> primaryStockComboBox = new JComboBox<>();
        primaryStockComboBox.addItem(new StockableComponentWrapper(null)); // "None" option
        StockableComponent currentPrimary = null;
        for (StockableComponent sc : allComponents) {
            primaryStockComboBox.addItem(new StockableComponentWrapper(sc));
            if (sc.getId().equals(foodItem.getPrimaryStockComponentId())) {
                currentPrimary = sc;
            }
        }
        if (currentPrimary != null) {
            primaryStockComboBox.setSelectedItem(new StockableComponentWrapper(currentPrimary));
        } else {
            primaryStockComboBox.setSelectedIndex(0); // Select "None"
        }
        gbcBasic.gridx = 1; gbcBasic.gridy = 2; gbcBasic.fill = GridBagConstraints.HORIZONTAL; gbcBasic.weightx = 1;
        basicInfoPanel.add(primaryStockComboBox, gbcBasic);
        
        // --- Recipe Panel ---
        JPanel recipePanel = new JPanel(new BorderLayout(5,5));
        recipePanel.setBorder(BorderFactory.createTitledBorder("Recipe Ingredients (from Stockable Components)"));
        
        DefaultTableModel recipeTableModel = new DefaultTableModel(new Object[]{"Ingredient", "Qty per Item", "Component ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Not directly editable
        };
        JTable recipeTable = new JTable(recipeTableModel);
        recipeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Populate recipe table
        if (foodItem.getRecipe() != null) {
            for (RecipeItem ri : foodItem.getRecipe()) {
                StockableComponent sc = menu.getStockableComponentById(ri.getComponentId());
                recipeTableModel.addRow(new Object[]{
                    sc != null ? sc.getName() : "Unknown Component",
                    ri.getQuantityPerItem(),
                    ri.getComponentId()
                });
            }
        }
        recipePanel.add(new JScrollPane(recipeTable), BorderLayout.CENTER);

        JPanel recipeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addIngredientButton = new JButton("Add Ingredient");
        JButton editIngredientButton = new JButton("Edit Ingredient");
        JButton removeIngredientButton = new JButton("Remove Ingredient");
        recipeButtonsPanel.add(addIngredientButton);
        recipeButtonsPanel.add(editIngredientButton);
        recipeButtonsPanel.add(removeIngredientButton);
        recipePanel.add(recipeButtonsPanel, BorderLayout.SOUTH);

        // --- Main Dialog Panel ---
        JPanel mainDialogPanel = new JPanel(new BorderLayout(10,10));
        mainDialogPanel.add(basicInfoPanel, BorderLayout.NORTH);
        mainDialogPanel.add(recipePanel, BorderLayout.CENTER);
        mainDialogPanel.setPreferredSize(new Dimension(550, 400)); // Give it more space


        // Action Listeners for Recipe Buttons
        addIngredientButton.addActionListener(e -> {
            addOrEditRecipeItemDialog(null, recipeTableModel, allComponents);
        });

        editIngredientButton.addActionListener(e -> {
            int selectedRow = recipeTable.getSelectedRow();
            if (selectedRow != -1) {
                String componentId = (String) recipeTableModel.getValueAt(selectedRow, 2);
                int quantity = (Integer) recipeTableModel.getValueAt(selectedRow, 1);
                RecipeItem existingRI = new RecipeItem(componentId, quantity);
                addOrEditRecipeItemDialog(existingRI, recipeTableModel, allComponents);
            } else {
                JOptionPane.showMessageDialog(mainDialogPanel, "Select an ingredient to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        removeIngredientButton.addActionListener(e -> {
            int selectedRow = recipeTable.getSelectedRow();
            if (selectedRow != -1) {
                recipeTableModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(mainDialogPanel, "Select an ingredient to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });


        String nameText = "";
        String costText = "";
        double newFoodItemCost = 0;
        boolean keepAsking = true;
        while (keepAsking) {
            int result = JOptionPane.showConfirmDialog(this, mainDialogPanel, "Modify Food Item",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            nameText = nameField.getText().trim();
            costText = costField.getText().trim();

            if (result == JOptionPane.OK_OPTION) {
                // Validate the name input
                if (nameText.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid name.");
                    continue;
                }
                // Validate the cost input
                try {
                    newFoodItemCost = Double.parseDouble(costText);
                    if (newFoodItemCost < 0) {
                        JOptionPane.showMessageDialog(this, "Cost cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid cost.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                keepAsking = false;
            } else {
                return; // User cancelled
            }
        }

        foodItem.setName(nameText);
        foodItem.setPrice(newFoodItemCost);

        // Update primary stock component
        StockableComponentWrapper selectedPrimaryWrapper = (StockableComponentWrapper) primaryStockComboBox.getSelectedItem();
        if (selectedPrimaryWrapper != null && selectedPrimaryWrapper.getComponent() != null) {
            foodItem.setPrimaryStockComponentId(selectedPrimaryWrapper.getComponent().getId());
        } else {
            foodItem.setPrimaryStockComponentId(null); // "None" selected
        }

        // Update recipe
        ArrayList<RecipeItem> newRecipe = new ArrayList<>();
        for (int i = 0; i < recipeTableModel.getRowCount(); i++) {
            String componentId = (String) recipeTableModel.getValueAt(i, 2);
            int quantity = (Integer) recipeTableModel.getValueAt(i, 1);
            newRecipe.add(new RecipeItem(componentId, quantity));
        }
        foodItem.setRecipe(newRecipe);

        // Update table display (if visible and selected)
        if (foodTable.getSelectedRow() != -1) {
            foodTableModel.setValueAt(foodItem, foodTable.getSelectedRow(), 0);
            foodTableModel.setValueAt(foodItem.getPrice(), foodTable.getSelectedRow(), 1);
            // foodTableModel.setValueAt(foodItem.getID(), foodTable.getSelectedRow(), 2); // ID doesn't change
        }
        
        menu.saveMenu();
        // menu.sendData(foodItem); // Consider if this needs to send more complex data or if client reconstructs
        System.out.println("FoodItem " + foodItem.toString() + " modified. PrimaryStockID: " + foodItem.getPrimaryStockComponentId() + ", Recipe items: " + foodItem.getRecipe().size());
    }

    // Helper class for JComboBox with StockableComponent
    private static class StockableComponentWrapper {
        private StockableComponent component;
        public StockableComponentWrapper(StockableComponent component) {
            this.component = component;
        }
        public StockableComponent getComponent() { return component; }
        @Override public String toString() { return component != null ? component.getName() + " (ID: " + component.getId().substring(0, Math.min(8, component.getId().length())) + "...)" : "None"; }
        
        @Override 
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            StockableComponentWrapper other = (StockableComponentWrapper) obj;
            if (this.component == null) {
                return other.component == null; // True if both are "None" (wrapper for null component)
            }
            // If this.component is not null, other.component must also not be null and their IDs must match
            return other.component != null && this.component.getId().equals(other.component.getId());
        }

        @Override 
        public int hashCode() { 
            // Consistent with equals: if component is null, hashCode is 0, otherwise based on ID.
            return component != null ? component.getId().hashCode() : 0; 
        }
    }

    private void addOrEditRecipeItemDialog(RecipeItem existingRecipeItem, DefaultTableModel recipeTableModel, ArrayList<StockableComponent> allComponents) {
        JComboBox<StockableComponentWrapper> ingredientComboBox = new JComboBox<>();
        ingredientComboBox.addItem(new StockableComponentWrapper(null)); // Placeholder for selection
         for (StockableComponent sc : allComponents) {
            ingredientComboBox.addItem(new StockableComponentWrapper(sc));
        }

        SpinnerNumberModel quantityModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        JSpinner quantitySpinner = new JSpinner(quantityModel);

        if (existingRecipeItem != null) {
            StockableComponent currentIngredient = menu.getStockableComponentById(existingRecipeItem.getComponentId());
            if (currentIngredient != null) {
                ingredientComboBox.setSelectedItem(new StockableComponentWrapper(currentIngredient));
            }
            quantitySpinner.setValue(existingRecipeItem.getQuantityPerItem());
        } else {
            ingredientComboBox.setSelectedIndex(0); // Ensure placeholder is selected initially
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Ingredient (Component):"));
        panel.add(ingredientComboBox);
        panel.add(new JLabel("Quantity per Food Item:"));
        panel.add(quantitySpinner);

        String dialogTitle = existingRecipeItem == null ? "Add Ingredient to Recipe" : "Edit Recipe Ingredient";
        int result = JOptionPane.showConfirmDialog(this, panel, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            StockableComponentWrapper selectedWrapper = (StockableComponentWrapper) ingredientComboBox.getSelectedItem();
            if (selectedWrapper == null || selectedWrapper.getComponent() == null) {
                JOptionPane.showMessageDialog(this, "Please select a valid ingredient component.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            StockableComponent selectedComponent = selectedWrapper.getComponent();
            int quantity = (Integer) quantitySpinner.getValue();

            if (existingRecipeItem != null) { // Editing existing
                int selectedRow = -1;
                for(int i=0; i < recipeTableModel.getRowCount(); i++){
                    if(recipeTableModel.getValueAt(i, 2).equals(existingRecipeItem.getComponentId())){
                        selectedRow = i;
                        break;
                    }
                }
                if(selectedRow != -1){
                    recipeTableModel.setValueAt(selectedComponent.getName(), selectedRow, 0);
                    recipeTableModel.setValueAt(quantity, selectedRow, 1);
                    recipeTableModel.setValueAt(selectedComponent.getId(), selectedRow, 2);
                }
            } else { // Adding new
                // Check if already in recipe
                for(int i=0; i < recipeTableModel.getRowCount(); i++){
                    if(recipeTableModel.getValueAt(i, 2).equals(selectedComponent.getId())){
                         JOptionPane.showMessageDialog(this, "This ingredient is already in the recipe. Edit it instead.", "Duplicate Ingredient", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                recipeTableModel.addRow(new Object[]{selectedComponent.getName(), quantity, selectedComponent.getId()});
            }
        }
    }


    public void modifyModifier(Modifier modifier) {
        JPanel customMessage = new JPanel(new GridLayout(0, 2, 5, 5)); // Changed to GridBagLayout for better alignment
        GridBagConstraints gbcMod = new GridBagConstraints();
        gbcMod.insets = new java.awt.Insets(2,2,2,2);
        gbcMod.anchor = GridBagConstraints.WEST;
        
        gbcMod.gridx = 0; gbcMod.gridy = 0;
        customMessage.add(new JLabel("Modifier Name:"), gbcMod);
        JTextField nameField = new JTextField(modifier.toString(), 20);
        gbcMod.gridx = 1; gbcMod.gridy = 0; gbcMod.fill = GridBagConstraints.HORIZONTAL; gbcMod.weightx = 1;
        customMessage.add(nameField, gbcMod);

        gbcMod.gridx = 0; gbcMod.gridy = 1; gbcMod.fill = GridBagConstraints.NONE; gbcMod.weightx = 0;
        customMessage.add(new JLabel("Additional Cost:"), gbcMod);
        JTextField costField = new JTextField(((Double) modifier.getAddCost()).toString(), 10);
        gbcMod.gridx = 1; gbcMod.gridy = 1; gbcMod.fill = GridBagConstraints.HORIZONTAL; gbcMod.weightx = 1;
        customMessage.add(costField, gbcMod);

        // --- Stock Component for Modifier ---
        gbcMod.gridx = 0; gbcMod.gridy = 2; gbcMod.fill = GridBagConstraints.NONE; gbcMod.weightx = 0;
        customMessage.add(new JLabel("Stock Component:"), gbcMod);
        ArrayList<StockableComponent> allComponentsMod = menu.getAllStockableComponents();
        JComboBox<StockableComponentWrapper> stockComboBoxMod = new JComboBox<>();
        stockComboBoxMod.addItem(new StockableComponentWrapper(null)); // "None" option
        StockableComponent currentStockCompMod = null;
        for (StockableComponent sc : allComponentsMod) {
            stockComboBoxMod.addItem(new StockableComponentWrapper(sc));
            if (sc.getId().equals(modifier.getStockComponentId())) {
                currentStockCompMod = sc;
            }
        }
        if (currentStockCompMod != null) {
            stockComboBoxMod.setSelectedItem(new StockableComponentWrapper(currentStockCompMod));
        } else {
            stockComboBoxMod.setSelectedIndex(0); // Select "None"
        }
        gbcMod.gridx = 1; gbcMod.gridy = 2; gbcMod.fill = GridBagConstraints.HORIZONTAL; gbcMod.weightx = 1;
        customMessage.add(stockComboBoxMod, gbcMod);

        // --- Quantity Effect for Modifier ---
        gbcMod.gridx = 0; gbcMod.gridy = 3; gbcMod.fill = GridBagConstraints.NONE; gbcMod.weightx = 0;
        customMessage.add(new JLabel("Stock Quantity Effect:"), gbcMod);
        SpinnerNumberModel qtyEffectModel = new SpinnerNumberModel(modifier.getQuantityEffect(), -10, 10, 1); // Current, Min, Max, Step
        JSpinner quantityEffectSpinner = new JSpinner(qtyEffectModel);
        gbcMod.gridx = 1; gbcMod.gridy = 3; gbcMod.fill = GridBagConstraints.HORIZONTAL; gbcMod.weightx = 1;
        customMessage.add(quantityEffectSpinner, gbcMod);
        
        customMessage.setPreferredSize(new Dimension(450, 180)); // Increased height for new field


        String nameText = "";
        String costText = "";
        double newModifierCost = 0;
        boolean keepAsking = true;
        while (keepAsking) {
            int result = JOptionPane.showConfirmDialog(this, customMessage, "Modify Modifier",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            nameText = nameField.getText().trim();
            costText = costField.getText().trim();
            if (result == JOptionPane.OK_OPTION) {
                // Validate the name input
                if (nameText.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid name.");
                    continue;
                }
                // Validate the cost input
                try {
                    newModifierCost = Double.parseDouble(costText);
                     if (newModifierCost < 0) {
                        JOptionPane.showMessageDialog(this, "Cost cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid cost.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                keepAsking = false;
            } else {
                return; // User cancelled
            }
        }
        modifier.setName(nameText);
        modifier.setAddCost(newModifierCost);
        modifier.setQuantityEffect((Integer) quantityEffectSpinner.getValue());

        // Update stock component
        StockableComponentWrapper selectedStockWrapperMod = (StockableComponentWrapper) stockComboBoxMod.getSelectedItem();
        if (selectedStockWrapperMod != null && selectedStockWrapperMod.getComponent() != null) {
            modifier.setStockComponentId(selectedStockWrapperMod.getComponent().getId());
        } else {
            modifier.setStockComponentId(null); // "None" selected
        }

        if (modifiersTable.getSelectedRow() != -1) {
            modifiersTableModel.setValueAt(modifier, modifiersTable.getSelectedRow(), 0);
            modifiersTableModel.setValueAt(modifier.getAddCost(), modifiersTable.getSelectedRow(), 1);
            // modifiersTableModel.setValueAt(modifier.getID(), modifiersTable.getSelectedRow(), 2); // ID doesn't change
        }
        menu.saveMenu();
        // menu.sendData(modifier); // Consider if this needs to send more complex data
        System.out.println("Modifier " + modifier.toString() + " modified. StockComponentID: " + modifier.getStockComponentId());
    }

    // Creates dialog window for adding categories and updates the database
    public void addCategory() {
        JPanel customMessage = new JPanel(new GridLayout(0, 1));
        JTextField nameField = new JTextField(25);

        customMessage.add(new JLabel("What is the name of this new category?"));
        customMessage.add(nameField);

        String nameText = "";
        boolean keepAsking = true;
        while (keepAsking) {
            int result = JOptionPane.showConfirmDialog(null, customMessage, "Add Category",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            nameText = nameField.getText();
            if (result == JOptionPane.OK_OPTION) {
                // Validate the name input
                if (nameText.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid name.");
                    continue;
                }
                keepAsking = false;
            } else {
                return;
            }
        }
        Category newCategory = new Category(nameText);
        menu.addCategory(newCategory);
        menu.saveMenu();
        menu.sendData(newCategory);
        Object[] newCategoryRow = new Object[2];
        newCategoryRow[0] = newCategory;
        newCategoryRow[1] = newCategory.getID();
        categoriesTableModel.addRow(newCategoryRow);
    }

    public void addFoodItem() {
        JPanel customMessage = new JPanel(new GridLayout(0, 1));
        JTextField nameField = new JTextField(25);
        JTextField costField = new JTextField(25);

        customMessage.add(new JLabel("What is the name of this new food item?"));
        customMessage.add(nameField);
        customMessage.add(new JLabel("What is the cost of this new food item?"));
        customMessage.add(costField);

        String nameText = "";
        String costText = "";
        double newFoodItemCost = 0;
        boolean keepAsking = true;
        while (keepAsking) {
            int result = JOptionPane.showConfirmDialog(null, customMessage, "Add Food Item",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            nameText = nameField.getText();
            costText = costField.getText();
            if (result == JOptionPane.OK_OPTION) {
                // Validate the name input
                if (nameText.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid name.");
                    continue;
                }
                // Validate the cost input
                try {
                    newFoodItemCost = Double.parseDouble(costText);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid cost.");
                    continue;
                }
                keepAsking = false;
            } else {
                return;
            }
        }

        Category sourceCategory = getSelectedCategory();
        FoodItem newFoodItem = new FoodItem(sourceCategory.getID(), nameField.getText());
        newFoodItem.setPrice(newFoodItemCost);
        sourceCategory.addFoodItem(newFoodItem);
        menu.saveMenu();
        menu.sendData(newFoodItem);
        Object[] newFoodRow = new Object[3];
        newFoodRow[0] = newFoodItem;
        newFoodRow[1] = newFoodItemCost;
        newFoodRow[2] = newFoodItem.getID();
        foodTableModel.addRow(newFoodRow);
    }

    public void addModifier() {
        JPanel customMessage = new JPanel(new GridBagLayout()); // Changed to GridBagLayout
        GridBagConstraints gbcAddMod = new GridBagConstraints();
        gbcAddMod.insets = new java.awt.Insets(2,2,2,2);
        gbcAddMod.anchor = GridBagConstraints.WEST;

        gbcAddMod.gridx = 0; gbcAddMod.gridy = 0;
        customMessage.add(new JLabel("Modifier Name:"), gbcAddMod);
        JTextField nameField = new JTextField(20);
        gbcAddMod.gridx = 1; gbcAddMod.gridy = 0; gbcAddMod.fill = GridBagConstraints.HORIZONTAL; gbcAddMod.weightx = 1;
        customMessage.add(nameField, gbcAddMod);

        gbcAddMod.gridx = 0; gbcAddMod.gridy = 1; gbcAddMod.fill = GridBagConstraints.NONE; gbcAddMod.weightx = 0;
        customMessage.add(new JLabel("Additional Cost:"), gbcAddMod);
        JTextField costField = new JTextField(10);
        gbcAddMod.gridx = 1; gbcAddMod.gridy = 1; gbcAddMod.fill = GridBagConstraints.HORIZONTAL; gbcAddMod.weightx = 1;
        customMessage.add(costField, gbcAddMod);

        // --- Stock Component for Modifier ---
        gbcAddMod.gridx = 0; gbcAddMod.gridy = 2; gbcAddMod.fill = GridBagConstraints.NONE; gbcAddMod.weightx = 0;
        customMessage.add(new JLabel("Stock Component (Optional):"), gbcAddMod);
        ArrayList<StockableComponent> allComponentsModAdd = menu.getAllStockableComponents();
        JComboBox<StockableComponentWrapper> stockComboBoxModAdd = new JComboBox<>();
        stockComboBoxModAdd.addItem(new StockableComponentWrapper(null)); // "None" option
        for (StockableComponent sc : allComponentsModAdd) {
            stockComboBoxModAdd.addItem(new StockableComponentWrapper(sc));
        }
        stockComboBoxModAdd.setSelectedIndex(0); // Default to "None"
        gbcAddMod.gridx = 1; gbcAddMod.gridy = 2; gbcAddMod.fill = GridBagConstraints.HORIZONTAL; gbcAddMod.weightx = 1;
        customMessage.add(stockComboBoxModAdd, gbcAddMod);

        // --- Quantity Effect for Modifier ---
        gbcAddMod.gridx = 0; gbcAddMod.gridy = 3; gbcAddMod.fill = GridBagConstraints.NONE; gbcAddMod.weightx = 0;
        customMessage.add(new JLabel("Stock Quantity Effect:"), gbcAddMod);
        SpinnerNumberModel qtyEffectModelAdd = new SpinnerNumberModel(0, -10, 10, 1); // Default 0, Min, Max, Step
        JSpinner quantityEffectSpinnerAdd = new JSpinner(qtyEffectModelAdd);
        gbcAddMod.gridx = 1; gbcAddMod.gridy = 3; gbcAddMod.fill = GridBagConstraints.HORIZONTAL; gbcAddMod.weightx = 1;
        customMessage.add(quantityEffectSpinnerAdd, gbcAddMod);

        customMessage.setPreferredSize(new Dimension(450, 180));


        String nameText = "";
        String costText = "";
        double newModifierCost = 0;
        boolean keepAsking = true;
        while (keepAsking) {
            int result = JOptionPane.showConfirmDialog(null, customMessage, "Add Modifier",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            nameText = nameField.getText();
            costText = costField.getText();
            if (result == JOptionPane.OK_OPTION) {
                // Validate the name input
                if (nameText.isBlank()) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid name.");
                    continue;
                }
                // Validate the cost input
                try {
                    newModifierCost = Double.parseDouble(costText);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid cost.");
                    continue;
                }
                keepAsking = false;
            } else {
                return;
            }
        }

        Category sourceCategory = getSelectedCategory();
        FoodItem sourceFoodItem = getSelectedFoodItem();
        Modifier newModifier = new Modifier(sourceCategory.getID(), sourceFoodItem.getID(), nameText);
        newModifier.setAddCost(newModifierCost);
        newModifier.setQuantityEffect((Integer) quantityEffectSpinnerAdd.getValue());
        
        StockableComponentWrapper selectedStockWrapperAdd = (StockableComponentWrapper) stockComboBoxModAdd.getSelectedItem();
        if (selectedStockWrapperAdd != null && selectedStockWrapperAdd.getComponent() != null) {
            newModifier.setStockComponentId(selectedStockWrapperAdd.getComponent().getId());
        } else {
            newModifier.setStockComponentId(null); // "None" selected
        }

        sourceFoodItem.addModifier(newModifier);
        menu.saveMenu();
        menu.sendData(newModifier);
        Object[] newModifierRow = new Object[3];
        newModifierRow[0] = newModifier;
        newModifierRow[1] = newModifierCost;
        newModifierRow[2] = newModifier.getID();
        modifiersTableModel.addRow(newModifierRow);
    }

    public Category getSelectedCategory() {
        if (categoriesTable.getSelectedRow() != -1) {
            return (Category) categoriesTable.getValueAt(categoriesTable.getSelectedRow(), 0);
        } else {
            return null;
        }
    }

    public FoodItem getSelectedFoodItem() {
        if (categoriesTable.getSelectedRow() != -1 && foodTable != null) {
            return (FoodItem) foodTable.getValueAt(foodTable.getSelectedRow(), 0);
        } else {
            return null;
        }
    }

    public Modifier getSelectedModifier() {
        if (categoriesTable.getSelectedRow() != -1 && foodTable.getSelectedRow() != -1 && modifiersTable != null) {
            return (Modifier) modifiersTable.getValueAt(modifiersTable.getSelectedRow(), 0);
        } else {
            return null;
        }
    }

    public void removeCategory(int rowIndex) {
        if (rowIndex != -1) {
            Category category = getSelectedCategory();
            categoriesTableModel.removeRow(rowIndex);
            menu.getCategories().remove(category);
            closeFoodItems();
            closeModifiers();
            menu.saveMenu();
            menu.sendData(category);
            detoggleOpenFoodButton();
        }
    }

    public void removeFoodItem(int rowIndex) {
        if (rowIndex != -1) {
            FoodItem foodItem = getSelectedFoodItem();
            foodTableModel.removeRow(rowIndex);
            Category sourceCategory = getSelectedCategory();
            sourceCategory.getFoodItems().remove(foodItem);
            closeModifiers();
            menu.saveMenu();
            menu.sendData(foodItem);
            detoggleOpenModifiersButton();
        }
    }

    public void removeModifier(int rowIndex) {
        if (rowIndex != -1) {
            Modifier modifier = getSelectedModifier();
            modifiersTableModel.removeRow(rowIndex);
            FoodItem sourceFoodItem = getSelectedFoodItem();
            sourceFoodItem.getModifiers().remove(modifier);
            menu.saveMenu();
            menu.sendData(modifier);
        }
    }

    public DefaultTableModel getCategoriesTableModel() {
        return this.categoriesTableModel;
    }

    public DefaultTableModel getFoodTableModel() {
        return this.foodTableModel;
    }

    public DefaultTableModel getModifiersTableModel() {
        return this.modifiersTableModel;
    }

    public boolean isShowingFoodItems() {
        return openFoodButton != null && openFoodButton.isSelected();
    }

    public boolean isShowingModifiers() {
        return openModifiersButton != null && openModifiersButton.isSelected();
    }

    public void detoggleOpenFoodButton() {
        openFoodButton.setSelected(false);
        openFoodButton.setText("Open Food Items");
    }

    public void detoggleOpenModifiersButton() {
        openModifiersButton.setSelected(false);
        openModifiersButton.setText("Open Modifiers");
    }

    // --- Methods for Inventory Components Tab ---

    private void initializeInventoryComponentsPanel() {
        stockableComponentsPanel.removeAll(); // Clear previous content if any

        // Table for Stockable Components
        createStockableComponentsTable(); // Initializes table and model
        stockableComponentsScroll = new JScrollPane(stockableComponentsTable);
        stockableComponentsTable.setFillsViewportHeight(true);
        stockableComponentsPanel.add(stockableComponentsScroll, BorderLayout.CENTER);

        // Buttons Panel for Stockable Components
        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(2, 2, 2, 2); // Padding

        JButton addButton = new JButton("Add Component");
        addButton.addActionListener(e -> addStockableComponentDialog());
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        buttonsPanel.add(addButton, gbc);

        JButton editButton = new JButton("Edit Selected Component");
        editButton.addActionListener(e -> {
            StockableComponent selected = getSelectedStockableComponent();
            if (selected != null) {
                modifyStockableComponentDialog(selected);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a component to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        gbc.gridy = 1;
        buttonsPanel.add(editButton, gbc);

        JButton deleteButton = new JButton("Delete Selected Component");
        deleteButton.addActionListener(e -> deleteStockableComponentAction());
        gbc.gridy = 2;
        buttonsPanel.add(deleteButton, gbc);

        // Add Stock Button
        JButton addStockButton = new JButton("Add Stock to Selected");
        addStockButton.addActionListener(e -> {
            StockableComponent selected = getSelectedStockableComponent();
            if (selected != null) {
                if (!selected.isTrackStock()) {
                    JOptionPane.showMessageDialog(this, "Stock is not tracked for component: " + selected.getName(), "Stock Not Tracked", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String amountStr = JOptionPane.showInputDialog(this, "Enter quantity to add:", "Add Stock", JOptionPane.PLAIN_MESSAGE);
                if (amountStr != null) {
                    try {
                        int amountToAdd = Integer.parseInt(amountStr);
                        if (amountToAdd > 0) {
                            selected.incrementStock(amountToAdd);
                            menu.updateStockableComponent(selected);
                            menu.saveMenu();
                            refreshStockableComponentsTable();
                            // Reselect the item after refresh
                            for(int i=0; i < stockableComponentsTable.getRowCount(); i++){
                                if(stockableComponentsTable.getValueAt(i, 0).equals(selected.getId())){
                                    stockableComponentsTable.setRowSelectionInterval(i, i);
                                    break;
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Please enter a positive quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid quantity entered. Please enter a number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a component.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        gbc.gridy = 3; gbc.weightx = 1; gbc.weighty = 0; // Reset weighty
        buttonsPanel.add(addStockButton, gbc);

        // Remove Stock Button
        JButton removeStockButton = new JButton("Remove Stock from Selected");
        removeStockButton.addActionListener(e -> {
            StockableComponent selected = getSelectedStockableComponent();
            if (selected != null) {
                if (!selected.isTrackStock()) {
                    JOptionPane.showMessageDialog(this, "Stock is not tracked for component: " + selected.getName(), "Stock Not Tracked", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                String amountStr = JOptionPane.showInputDialog(this, "Enter quantity to remove:", "Remove Stock", JOptionPane.PLAIN_MESSAGE);
                if (amountStr != null) {
                    try {
                        int amountToRemove = Integer.parseInt(amountStr);
                        if (amountToRemove > 0) {
                            if (selected.getStockQuantity() < amountToRemove) {
                                // Optional: Ask if user wants to set to 0 or just show error
                                JOptionPane.showMessageDialog(this, "Cannot remove " + amountToRemove + ". Only " + selected.getStockQuantity() + " available. Stock will be set to 0 if you proceed with a larger or equal amount.", "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                                // For simplicity, decrement will cap at 0 anyway due to StockableComponent logic
                            }
                            selected.decrementStock(amountToRemove);
                            menu.updateStockableComponent(selected);
                            menu.saveMenu();
                            refreshStockableComponentsTable();
                            // Reselect the item after refresh
                             for(int i=0; i < stockableComponentsTable.getRowCount(); i++){
                                if(stockableComponentsTable.getValueAt(i, 0).equals(selected.getId())){
                                    stockableComponentsTable.setRowSelectionInterval(i, i);
                                    break;
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Please enter a positive quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid quantity entered. Please enter a number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a component.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        gbc.gridy = 4;
        buttonsPanel.add(removeStockButton, gbc);
        
        gbc.gridy = 5; gbc.weighty = 1; // Push other buttons up
        buttonsPanel.add(new JPanel(), gbc);


        stockableComponentsPanel.add(buttonsPanel, BorderLayout.EAST);
        stockableComponentsPanel.revalidate();
        stockableComponentsPanel.repaint();
    }

    private void createStockableComponentsTable() {
        ArrayList<StockableComponent> components = menu.getAllStockableComponents();
        Object[] columnNames = {"ID", "Name", "Current Stock", "Track Stock"};
        Object[][] data = new Object[components.size()][4];

        for (int i = 0; i < components.size(); i++) {
            StockableComponent component = components.get(i);
            data[i][0] = component.getId();
            data[i][1] = component.getName();
            data[i][2] = component.getStockQuantity();
            data[i][3] = component.isTrackStock();
        }

        stockableComponentsTableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) { // "Track Stock" column
                    return Boolean.class;
                }
                if (columnIndex == 2) { // "Current Stock" column
                    return Integer.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Make Track Stock and Current Stock editable directly in the table for quick changes
                // return column == 2 || column == 3; 
                // For now, let's keep it non-editable directly, use dialogs for consistency
                return false; 
            }
        };

        stockableComponentsTable = new JTable(stockableComponentsTableModel);
        stockableComponentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Optional: Add cell editor for checkbox if making it editable directly
        // stockableComponentsTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        // stockableComponentsTable.getColumnModel().getColumn(3).setCellRenderer(stockableComponentsTable.getDefaultRenderer(Boolean.class));


        // Listener for table changes if cells were editable
        /*
        stockableComponentsTableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (row >= 0 && column >= 0) {
                    StockableComponent component = menu.getAllStockableComponents().get(row);
                    Object newValue = stockableComponentsTableModel.getValueAt(row, column);
                    boolean changed = false;
                    if (column == 2) { // Current Stock
                        try {
                            int newStock = Integer.parseInt(newValue.toString());
                            if (component.getStockQuantity() != newStock) {
                                component.setStockQuantity(newStock);
                                changed = true;
                            }
                        } catch (NumberFormatException ex) {
                            // Handle error or revert
                        }
                    } else if (column == 3) { // Track Stock
                        boolean newTrackStatus = (Boolean) newValue;
                        if (component.isTrackStock() != newTrackStatus) {
                            component.setTrackStock(newTrackStatus);
                            changed = true;
                        }
                    }
                    if (changed) {
                        menu.updateStockableComponent(component);
                        menu.saveMenu();
                    }
                }
            }
        });
        */
    }
    
    private void refreshStockableComponentsTable() {
        ArrayList<StockableComponent> components = menu.getAllStockableComponents();
        Object[] columnNames = {"ID", "Name", "Current Stock", "Track Stock"};
        Object[][] data = new Object[components.size()][4];

        for (int i = 0; i < components.size(); i++) {
            StockableComponent component = components.get(i);
            data[i][0] = component.getId();
            data[i][1] = component.getName();
            data[i][2] = component.getStockQuantity();
            data[i][3] = component.isTrackStock();
        }
        stockableComponentsTableModel.setDataVector(data, columnNames);
    }


    private StockableComponent getSelectedStockableComponent() {
        int selectedRow = stockableComponentsTable.getSelectedRow();
        if (selectedRow != -1) {
            // Assuming the table is not sorted/filtered in a way that misaligns with the list
            return menu.getAllStockableComponents().get(selectedRow);
        }
        return null;
    }

    private void addStockableComponentDialog() {
        JTextField nameField = new JTextField(20);
        SpinnerNumberModel stockModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        JSpinner stockSpinner = new JSpinner(stockModel);
        JCheckBox trackStockCheckBox = new JCheckBox("Track Stock", true);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Component Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Initial Stock Quantity:"));
        panel.add(stockSpinner);
        panel.add(new JLabel("")); // Spacer
        panel.add(trackStockCheckBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Stockable Component",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Component name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int stockQuantity = (Integer) stockSpinner.getValue();
            boolean trackStock = trackStockCheckBox.isSelected();

            StockableComponent newComponent = new StockableComponent(name, stockQuantity, trackStock);
            menu.addStockableComponent(newComponent);
            menu.saveMenu();
            refreshStockableComponentsTable();
        }
    }

    private void modifyStockableComponentDialog(StockableComponent component) {
        JTextField nameField = new JTextField(component.getName(), 20);
        SpinnerNumberModel stockModel = new SpinnerNumberModel(component.getStockQuantity(), 0, Integer.MAX_VALUE, 1);
        JSpinner stockSpinner = new JSpinner(stockModel);
        JCheckBox trackStockCheckBox = new JCheckBox("Track Stock", component.isTrackStock());

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Component Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Stock Quantity:"));
        panel.add(stockSpinner);
        panel.add(new JLabel("")); // Spacer
        panel.add(trackStockCheckBox);
        
        // Display ID (non-editable)
        panel.add(new JLabel("Component ID:"));
        JTextField idField = new JTextField(component.getId());
        idField.setEditable(false);
        panel.add(idField);


        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Stockable Component",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Component name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            component.setName(name);
            component.setStockQuantity((Integer) stockSpinner.getValue());
            component.setTrackStock(trackStockCheckBox.isSelected());

            menu.updateStockableComponent(component);
            menu.saveMenu();
            refreshStockableComponentsTable();
        }
    }

    private void deleteStockableComponentAction() {
        StockableComponent selected = getSelectedStockableComponent();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a component to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if component is in use by any FoodItem or Modifier
        boolean isInUse = false;
        String usedByDetails = "";

        for (Category cat : menu.getCategories()) {
            for (FoodItem item : cat.getFoodItems()) {
                if (selected.getId().equals(item.getPrimaryStockComponentId())) {
                    isInUse = true;
                    usedByDetails += "\n- Food Item (Primary): " + item.toString() + " in category " + cat.toString();
                }
                for (RecipeItem ri : item.getRecipe()) {
                    if (selected.getId().equals(ri.getComponentId())) {
                        isInUse = true;
                        usedByDetails += "\n- Food Item (Recipe): " + item.toString() + " in category " + cat.toString();
                    }
                }
                for (Modifier mod : item.getModifiers()) {
                    if (selected.getId().equals(mod.getStockComponentId())) {
                        isInUse = true;
                        usedByDetails += "\n- Modifier: " + mod.toString() + " for item " + item.toString();
                    }
                }
            }
        }


        if (isInUse) {
            JOptionPane.showMessageDialog(this, 
                "Cannot delete component '" + selected.getName() + "' as it is currently in use by:" + usedByDetails +
                "\nPlease remove its associations before deleting.", 
                "Component In Use", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete component: " + selected.getName() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            menu.deleteStockableComponent(selected.getId());
            menu.saveMenu();
            refreshStockableComponentsTable();
        }
    }
}
