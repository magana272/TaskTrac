package task.trak.app.client.gui.view.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

public final class TableUtil {

    private TableUtil() {}

    public static JTable createCopyableTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        KeyStroke copy = KeyStroke.getKeyStroke("control C");
        KeyStroke macCopy = KeyStroke.getKeyStroke("meta C");
        Action copyAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (row >= 0 && col >= 0) {
                    Object val = table.getValueAt(row, col);
                    String text = val != null ? val.toString() : "";
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(text), null);
                }
            }
        };
        table.getInputMap().put(copy, "copy");
        table.getInputMap().put(macCopy, "copy");
        table.getActionMap().put("copy", copyAction);
        return table;
    }
}
