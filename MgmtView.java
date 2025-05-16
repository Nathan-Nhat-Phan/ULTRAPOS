
/** 
 * MgmtView.java
 * View Panel for management use
 * Uses: Viewing all orders and building/editing menu
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator; // Added for sorting ArrayList

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowSorter; // Added this import
import java.awt.Insets;
import java.awt.Dimension;


public class MgmtView extends JPanel {
    private GUI gui; // host GUI whose body is where this View is displayed
    private DefaultTableModel ordersTableModel; // GUI JTable-accessable orders data
    private JTable orderHistoryTable; // MgmtView's table of ALL orders
    private JScrollPane orderHistoryScrollPane; // allows table to be scrollable
    private JScrollPane menuScrollPane; // allows menu interface to be scrollable

    private TableRowSorter<DefaultTableModel> sorter; // handles row sorting/filtering
    private RowFilter<Object, Object> comboFilter; // combines search filters
    private ArrayList<RowFilter<Object, Object>> appliedFiltersList; // list of search filters to combine

    private MenuCreator menuCreator; // Menu creation interface panel

    // Sales Report Components
    private JPanel salesReportPanel;
    private JSpinner salesReportStartDateSpinner;
    private JSpinner salesReportEndDateSpinner;
    private JButton generateReportButton;
    private JTable salesReportTable;
    private DefaultTableModel salesReportTableModel;
    private JScrollPane salesReportScrollPane;
    private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy");

    // Helper class for storing sales data
    private static class FoodSale {
        String foodItemName;
        int quantitySold;

        FoodSale(String foodItemName, int quantitySold) {
            this.foodItemName = foodItemName;
            this.quantitySold = quantitySold;
        }

        void incrementQuantity(int amount) {
            this.quantitySold += amount;
        }

        String getFoodItemName() {
            return foodItemName;
        }

        int getQuantitySold() {
            return quantitySold;
        }
    }


    public MgmtView(GUI gui) {
        this.gui = gui;
        this.ordersTableModel = gui.getPOS().getOrdersTableModel();
        // setBackground(Color.BLUE);
        setLayout(new GridBagLayout());
        GridBagConstraints mainGBC = new GridBagConstraints(); // Use a more descriptive name

        // Order History Section
        mainGBC.gridx = 0;
        mainGBC.gridy = 0;
        mainGBC.weightx = 1.0;
        mainGBC.fill = GridBagConstraints.HORIZONTAL;
        mainGBC.anchor = GridBagConstraints.NORTHWEST;
        mainGBC.insets = new Insets(5, 5, 0, 5);
        JLabel orderViewLabel = new JLabel("View Order History");
        add(orderViewLabel, mainGBC);

        mainGBC.gridy = 1;
        mainGBC.weighty = 0.4; // Allocate space for order history
        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.insets = new Insets(0, 5, 5, 5);
        createOrderHistoryTable(); // This method adds the table and its search panel to `this` panel
        // The createOrderHistoryTable method itself adds components using its own GBC.
        // We need to ensure it adds them relative to the overall layout.
        // For now, assuming createOrderHistoryTable correctly adds its components.

        // Sales Report Section
        mainGBC.gridy = 2; // Position below order history
        mainGBC.weighty = 0.0; // Reset weighty for label
        mainGBC.fill = GridBagConstraints.HORIZONTAL;
        mainGBC.insets = new Insets(10, 5, 0, 5);
        JLabel salesReportLabel = new JLabel("Food Items Sold Report");
        add(salesReportLabel, mainGBC);
        
        mainGBC.gridy = 3;
        mainGBC.weighty = 0.3; // Allocate space for sales report
        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.insets = new Insets(0, 5, 5, 5);
        createSalesReportPanel();
        add(salesReportPanel, mainGBC);


        // Menu Creation Section
        mainGBC.gridy = 4; // Position below sales report
        mainGBC.weighty = 0.0; // Reset weighty for label
        mainGBC.fill = GridBagConstraints.HORIZONTAL;
        mainGBC.insets = new Insets(10, 5, 0, 5);
        JLabel menuCreationLabel = new JLabel("Menu Creation Interface");
        add(menuCreationLabel, mainGBC);

        mainGBC.gridy = 5;
        mainGBC.weighty = 0.3; // Allocate remaining space
        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.insets = new Insets(0, 5, 5, 5);
        menuCreator = new MenuCreator(gui);
        menuScrollPane = new JScrollPane(menuCreator);
        menuScrollPane.setPreferredSize(new Dimension(800, 400)); // Give it a preferred size
        add(menuScrollPane, mainGBC);
    }

    public void createOrderHistoryTable() {
        orderHistoryTable = new JTable(gui.getPOS().getOrdersTableModel());

        // Adjusted column widths for new columns
        orderHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Food
        orderHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(50);  // Qty
        orderHistoryTable.getColumnModel().getColumn(2).setPreferredWidth(600); // Special
        orderHistoryTable.getColumnModel().getColumn(3).setPreferredWidth(70); // Completion
        orderHistoryTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Date
        orderHistoryTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Completion Date
        orderHistoryTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Student ID
        orderHistoryTable.getColumnModel().getColumn(7).setPreferredWidth(50);  // ID
        orderHistoryTable.getTableHeader().setReorderingAllowed(false);

        orderHistoryTable.getColumnModel().getColumn(0).setCellRenderer(new NewLinesCellRenderer());
        orderHistoryTable.getColumnModel().getColumn(1).setCellRenderer(new NewLinesCellRenderer());
        orderHistoryTable.getColumnModel().getColumn(2).setCellRenderer(new NewLinesCellRenderer());
        // Student ID (column 6) and ID (column 7) might not need NewLinesCellRenderer/Editor
        // depending on expected content. Assuming they are single line for now.
        orderHistoryTable.getColumnModel().getColumn(0).setCellEditor(new NewLinesCellEditor());
        orderHistoryTable.getColumnModel().getColumn(1).setCellEditor(new NewLinesCellEditor());
        orderHistoryTable.getColumnModel().getColumn(2).setCellEditor(new NewLinesCellEditor());

        // orderHistoryTable.removeColumn(orderHistoryTable.getColumnModel().getColumn(5));
        // adjustRowHeights(ord);

        sorter = new TableRowSorter<DefaultTableModel>(ordersTableModel);
        orderHistoryTable.setRowSorter(sorter);

        appliedFiltersList = new ArrayList<RowFilter<Object, Object>>();
        updateTable();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.weightx = 1;
        // add(orderHistoryTable.getTableHeader(), gbc);

        gbc.weighty = 0.5;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        orderHistoryScrollPane = new JScrollPane(orderHistoryTable);
        orderHistoryTable.setFillsViewportHeight(true);
        add(new SearchPanel(sorter, comboFilter, appliedFiltersList, gui.getPOS().getOrders()), gbc);

        gbc.gridy = 2;
        add(orderHistoryScrollPane, gbc);

        NewLinesTableSetUp.setup(orderHistoryTable, orderHistoryScrollPane);

    }

    /*
     * private void adjustRowHeight(JTable table, int row, int column) {
     * int maxHeight = 0;
     * for (int i = 0; i < table.getColumnCount(); i++) {
     * TableCellRenderer renderer = table.getCellRenderer(row, i);
     * Object value = table.getValueAt(row, i);
     * Component comp = renderer.getTableCellRendererComponent(table, value, false,
     * false, row, i);
     * maxHeight = Math.max(comp.getPreferredSize().height, maxHeight);
     * }
     * orderHistoryTable.setRowHeight(row, maxHeight);
     * }
     */

    public JTable getOrderHistoryTable() {
        return orderHistoryTable;
    }

    public MenuCreator getMenuCreator() {
        return menuCreator;
    }

    public void updateTable() {
        // Combine all active filters
        comboFilter = RowFilter.andFilter(appliedFiltersList);

        // Apply the combined filter to the sorter
        sorter.setRowFilter(comboFilter);
    }

    private void createSalesReportPanel() {
        salesReportPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Start Date
        gbc.gridx = 0;
        gbc.gridy = 0;
        salesReportPanel.add(new JLabel("Start Date:"), gbc);

        gbc.gridx = 1;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7); // Default start date: 7 days ago
        Date defaultStartDate = cal.getTime();
        SpinnerDateModel startDateModel = new SpinnerDateModel(defaultStartDate, null, null, Calendar.DAY_OF_MONTH);
        salesReportStartDateSpinner = new JSpinner(startDateModel);
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(salesReportStartDateSpinner, "MMM dd, yyyy"); // Changed d to dd
        salesReportStartDateSpinner.setEditor(startDateEditor);
        salesReportPanel.add(salesReportStartDateSpinner, gbc);

        // End Date
        gbc.gridx = 2;
        salesReportPanel.add(new JLabel("End Date:"), gbc);

        gbc.gridx = 3;
        Date defaultEndDate = new Date(); // Default to today
        SpinnerDateModel endDateModel = new SpinnerDateModel(defaultEndDate, null, null, Calendar.DAY_OF_MONTH);
        salesReportEndDateSpinner = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(salesReportEndDateSpinner, "MMM dd, yyyy"); // Changed d to dd
        salesReportEndDateSpinner.setEditor(endDateEditor);
        salesReportPanel.add(salesReportEndDateSpinner, gbc);

        // Generate Button
        gbc.gridx = 4;
        gbc.anchor = GridBagConstraints.EAST;
        generateReportButton = new JButton("Generate Report");
        generateReportButton.addActionListener(e -> generateSalesReport());
        salesReportPanel.add(generateReportButton, gbc);

        // Sales Report Table
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        salesReportTableModel = new DefaultTableModel(new Object[]{"Food Item", "Total Quantity Sold"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return Integer.class; // For proper sorting of numbers
                }
                return String.class;
            }
        };
        salesReportTable = new JTable(salesReportTableModel);
        salesReportTable.setFillsViewportHeight(true);
        // Enable sorting for the sales report table
        TableRowSorter<DefaultTableModel> salesReportSorter = new TableRowSorter<>(salesReportTableModel);
        salesReportTable.setRowSorter(salesReportSorter);


        salesReportScrollPane = new JScrollPane(salesReportTable);
        salesReportPanel.add(salesReportScrollPane, gbc);
    }

    private void generateSalesReport() {
        Date startDate = (Date) salesReportStartDateSpinner.getValue();
        Date endDate = (Date) salesReportEndDateSpinner.getValue();

        // Ensure end date is not before start date
        if (endDate.before(startDate)) {
            // Optionally show an error message to the user
            System.err.println("End date cannot be before start date.");
            // Reset end date to start date or show a dialog
            salesReportEndDateSpinner.setValue(startDate);
            endDate = startDate;
            // return; // Or proceed with corrected date
        }

        Date normStartDate = normalizeDate(startDate, false);
        Date normEndDate = normalizeDate(endDate, true);

        List<Order> allOrders = gui.getPOS().getOrders();
        ArrayList<FoodSale> salesDataList = new ArrayList<>();

        for (Order order : allOrders) {
            if (order.getStatus() && order.getCompletionDate() != null) { // Only completed orders
                Date completionDate = Date.from(order.getCompletionDate().atZone(java.time.ZoneId.systemDefault()).toInstant());
                Date normCompletionDate = normalizeDate(completionDate, false);

                if (!normCompletionDate.before(normStartDate) && !normCompletionDate.after(normEndDate)) {
                    ArrayList<String> foodItems = order.getFood();
                    ArrayList<Integer> quantities = order.getQuantity();
                    for (int i = 0; i < foodItems.size(); i++) {
                        String foodItemName = foodItems.get(i);
                        int quantity = quantities.get(i);

                        // Update existing FoodSale or add new one
                        boolean found = false;
                        for (FoodSale sale : salesDataList) {
                            if (sale.getFoodItemName().equals(foodItemName)) {
                                sale.incrementQuantity(quantity);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            salesDataList.add(new FoodSale(foodItemName, quantity));
                        }
                    }
                }
            }
        }

        // Clear previous report data
        salesReportTableModel.setRowCount(0);

        // Sort the sales data by quantity sold in descending order
        salesDataList.sort(Comparator.comparingInt(FoodSale::getQuantitySold).reversed());

        // Populate table with new data
        for (FoodSale sale : salesDataList) {
            salesReportTableModel.addRow(new Object[]{sale.getFoodItemName(), sale.getQuantitySold()});
        }

        // After populating, if you want to sort by default (e.g., by quantity descending)
        // and have a TableRowSorter set on salesReportTable:
        TableRowSorter<?> sorter = (TableRowSorter<?>) salesReportTable.getRowSorter();
        if (sorter != null) {
            ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(1, javax.swing.SortOrder.DESCENDING)); // Sort by "Total Quantity Sold" (column 1)
            sorter.setSortKeys(sortKeys);
            sorter.sort();
        }
    }

    private Date normalizeDate(Date date, boolean toEndOfDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (toEndOfDay) {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal.getTime();
    }
}
