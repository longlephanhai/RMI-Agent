package rmi.client.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogPanel extends JPanel {
    private final JTable logTable;
    private final DefaultTableModel logTableModel;

    public LogPanel() {
        setLayout(new BorderLayout());
        String[] columns = {"Time", "Type", "Message"};
        logTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        logTable = new JTable(logTableModel);
        JScrollPane scroll = new JScrollPane(logTable);
        add(scroll, BorderLayout.CENTER);
    }

    public void appendLog(String type, String message) {
        SwingUtilities.invokeLater(() -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logTableModel.addRow(new Object[]{time, type, message});
            logTable.scrollRectToVisible(logTable.getCellRect(logTableModel.getRowCount() - 1, 0, true));
        });
    }

    public void clearLogs() {
        SwingUtilities.invokeLater(() -> logTableModel.setRowCount(0));
    }
}
