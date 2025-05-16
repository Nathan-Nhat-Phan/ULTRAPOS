
/** 
 * OrdersTableModel.java
 * Stores orders data in a 2D format
 * GUI (JTables) accesses order data through this
 */

import java.time.LocalDateTime;

import javax.swing.table.DefaultTableModel;

public class OrdersTableModel extends DefaultTableModel {

    private static final String[] orderHistoryColumnTitles = { "Food", "Qty", "Special", "Completion", "Date", "Completion Date", "Student ID", "ID" };

    public OrdersTableModel(POS pos) {
        super(OrdersPreparer.prepareOrders(pos.getOrders()), orderHistoryColumnTitles);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 1) {
            return Integer.class;
        }
        if (col == 3) {
            return Boolean.class;
        }
        if (col == 4) {
            return LocalDateTime.class;
        }
        if (col == 5) {
            return LocalDateTime.class; // Completion Date
        }
        if (col == 7) {
            return Integer.class; // ID
        } else {
            return super.getColumnClass(col);
        }
    }
}
