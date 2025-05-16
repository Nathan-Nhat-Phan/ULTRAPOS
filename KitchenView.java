
/** 
 * KitchenView.java
 * View Panel for kitchen use
 * Uses: Viewing incomplete orders received from the kitchen and marking orders as complete
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class KitchenView extends JPanel {
    private GUI gui; // host GUI whose body is where this View is displayed
    private DefaultTableModel ordersTableModel; // GUI JTable-accessable orders data
    private JTable currentOrdersTable; // KitchenView's table of pending orders
    private JScrollPane currentOrdersScrollPane; // allows table to be scrollable

    private TableRowSorter<DefaultTableModel> sorter; // handles row sorting/filtering
    private RowFilter<Object, Object> comboFilter; // combines search filters
    private ArrayList<RowFilter<Object, Object>> appliedFiltersList; // list of search filters to combine

    public KitchenView(GUI gui) {
        this.gui = gui;
        this.ordersTableModel = gui.getPOS().getOrdersTableModel();
        // setBackground(Color.GREEN);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel orderViewLabel = new JLabel("View Pending Orders");
        gbc.gridy = 0;
        add(orderViewLabel, gbc);

        gbc.gridy += 1;
        gbc.fill = GridBagConstraints.BOTH;
        createCurrentOrdersTable(gbc);
    }

    // Defines/renders KitchenView's table of pending orders
    public void createCurrentOrdersTable(GridBagConstraints gbc) {
        currentOrdersTable = new JTable(gui.getPOS().getOrdersTableModel());

        // Adjusted column widths for new columns
        currentOrdersTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Food
        currentOrdersTable.getColumnModel().getColumn(1).setPreferredWidth(50);  // Qty
        currentOrdersTable.getColumnModel().getColumn(2).setPreferredWidth(600); // Special
        currentOrdersTable.getColumnModel().getColumn(3).setPreferredWidth(70); // Completion
        currentOrdersTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Date
        currentOrdersTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Completion Date
        currentOrdersTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Student ID
        currentOrdersTable.getColumnModel().getColumn(7).setPreferredWidth(50);  // ID

        currentOrdersTable.getColumnModel().getColumn(0).setCellRenderer(new NewLinesCellRenderer());
        currentOrdersTable.getColumnModel().getColumn(1).setCellRenderer(new NewLinesCellRenderer());
        currentOrdersTable.getColumnModel().getColumn(2).setCellRenderer(new NewLinesCellRenderer());
        currentOrdersTable.getColumnModel().getColumn(0).setCellEditor(new NewLinesCellEditor());
        currentOrdersTable.getColumnModel().getColumn(1).setCellEditor(new NewLinesCellEditor());
        currentOrdersTable.getColumnModel().getColumn(2).setCellEditor(new NewLinesCellEditor());

        // Responsible for filtering out rows with "true" in the completion column
        // (complete orders)
        sorter = new TableRowSorter<DefaultTableModel>(ordersTableModel);
        currentOrdersTable.setRowSorter(sorter);

        appliedFiltersList = new ArrayList<RowFilter<Object, Object>>();

        RowFilter<Object, Object> completionFilter = RowFilter.regexFilter("false");
        appliedFiltersList.add(completionFilter);

        updateTable();

        // Refilters the currentOrdersTable every time the data changes
        ordersTableModel.addTableModelListener(e -> {
            if (ordersTableModel.getRowCount() > 0 && e.getFirstRow() < ordersTableModel.getRowCount() && e.getFirstRow() != -1 && e.getColumn() != -1) { // Add checks for valid row and column
                int col = e.getColumn();
                int row = e.getFirstRow();
                // Order ID is now at column 7
                Object orderIdObj = ordersTableModel.getValueAt(row, 7);
                if (orderIdObj == null) return; // if the row was deleted or ID is null

                int orderID = Integer.parseInt(orderIdObj.toString());
                int indexWithinOrdersTableModel = gui.getPOS().searchByOrderIDInOrdersTableModel(orderID);
                int indexWithinOrdersList = gui.getPOS().searchByOrderIDInOrdersList(orderID);

                if (indexWithinOrdersList == -1) return; // Order not found in list

                if (col == 3) // Check if the event is from the completion status checkbox column
                {
                    Boolean isChecked = Boolean.parseBoolean(ordersTableModel.getValueAt(row, col).toString());
                    gui.getPOS().getOrders().get(indexWithinOrdersList).setStatus(isChecked);
                    // The setStatus method in Order.java now handles setting the completionDate

                    Order order = gui.getPOS().getOrders().get(indexWithinOrdersList);
                    gui.getPOS().saveOrders();
                    gui.getPOS().sendData(order);
                    updateTable(); // Update table after status change
                }
            }
        });

        currentOrdersTable.getTableHeader().setReorderingAllowed(false);
        currentOrdersScrollPane = new JScrollPane(currentOrdersTable);
        currentOrdersTable.setFillsViewportHeight(true);

        add(new SearchPanel(sorter, comboFilter, appliedFiltersList, gui.getPOS().getOrders()), gbc);
        gbc.gridy += 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(currentOrdersScrollPane, gbc);
        NewLinesTableSetUp.setup(currentOrdersTable, currentOrdersScrollPane);
    }

    public JTable getCurrentOrdersTable() {
        return currentOrdersTable;
    }

    public void updateTable() {
        // Combine all active filters
        comboFilter = RowFilter.andFilter(appliedFiltersList);

        // Apply the combined filter to the sorter
        sorter.setRowFilter(comboFilter);
    }
}
