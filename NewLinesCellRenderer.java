
/** 
 * NewLinesCellRenderer.java
 * Custom cell renderer that supports multi-line text
 */

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;

public class NewLinesCellRenderer extends DefaultTableCellRenderer {

    private JTextArea textArea; // supports multiple liens

    NewLinesCellRenderer() {
        textArea = new JTextArea();
        textArea.setLineWrap(false);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        textArea.setText((value != null) ? value.toString() : "");
        setText((value == null) ? "" : value.toString());
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        return textArea;
    }
}