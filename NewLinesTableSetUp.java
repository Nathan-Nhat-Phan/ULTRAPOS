
/** 
 * NewLinesTableSetUp.java
 * Upon table creation, adjusts all row heights to fit their multi-line text
 */

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NewLinesTableSetUp {
    public static void setup(JTable table, JScrollPane scroll) {
        for (int row = 0; row < table.getRowCount(); row++) {
            NewLinesRowHeightAdjuster.adjustRowHeightForRow(table, row);
        }
        table.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                NewLinesRowHeightAdjuster.adjustRowHeightForRow(table, row);
            }
        });
        scroll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
            }
        });
    }
}
