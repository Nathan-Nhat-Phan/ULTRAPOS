
/** 
 * NewLinesCellEditor.java
 * Custom cell editor that supports multi-line text
 */

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class NewLinesCellEditor extends DefaultCellEditor {
    private JTextArea textArea; // supports multiple liens
    private JScrollPane scrollPane; // supports scrolling cell

    public NewLinesCellEditor() {
        super(new JTextField());
        textArea = new JTextArea();
        textArea.setLineWrap(false);
        textArea.setWrapStyleWord(true);
        scrollPane = new JScrollPane(textArea);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textArea.setText((value != null) ? value.toString() : "");
        NewLinesRowHeightAdjuster.adjustRowHeightForRow(table, row); // SwingUtilities.invokeLater(() -> )
        return scrollPane;
    }

    @Override
    public Object getCellEditorValue() {
        return textArea.getText();
    }
}
