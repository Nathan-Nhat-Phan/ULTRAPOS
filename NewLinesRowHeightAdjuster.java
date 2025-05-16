
/** 
 * NewLinesRowHeightAdjuster.java
 * Readjusts a table row's height to fit multi-line text in cells
 * Sets overall row height to the largest preferred height of a row's cells
 */

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class NewLinesRowHeightAdjuster {
    static void adjustRowHeightForRow(JTable table, int row) {
        if (row == -1 || row >= table.getRowCount())
            return; // No row is being edited

        int maxHeight = 0;
        for (int column = table.getColumnCount() - 1; column >= 0; column--) {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Object value = table.getValueAt(row, column);
            Component comp = renderer.getTableCellRendererComponent(table, value, true, true, row, column);
            maxHeight = Math.max(comp.getPreferredSize().height, maxHeight);
        }
        table.setRowHeight(row, maxHeight);
    }
}
