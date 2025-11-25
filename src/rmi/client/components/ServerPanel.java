package rmi.client.components;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ServerPanel extends JPanel {
    private final DefaultTableModel serverTableModel;
    private final Map<String, Integer> serverTasks = new HashMap<>();
    private String selectedServer = null;

    private ServerSelectionListener listener;

    public interface ServerSelectionListener {
        void serverSelected(String serverName);
    }

    public void setServerSelectionListener(ServerSelectionListener listener) {
        this.listener = listener;
    }

    public ServerPanel() {
        setLayout(new BorderLayout());
        String[] columns = {"Server Name", "Running Tasks"};
        serverTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable serverTable = new JTable(serverTableModel);
        serverTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Khi chọn 1 server
        serverTable.getSelectionModel().addListSelectionListener(e -> {
            int row = serverTable.getSelectedRow();
            if (row >= 0) {
                selectedServer = (String) serverTableModel.getValueAt(row, 0);
                if (listener != null) listener.serverSelected(selectedServer);
            }
        });

        JScrollPane scroll = new JScrollPane(serverTable);
        add(new JLabel("Servers"), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void updateServers(Map<String, Integer> tasks) {
        serverTasks.putAll(tasks);
        SwingUtilities.invokeLater(this::refreshTable);
    }

    private void refreshTable() {
        serverTableModel.setRowCount(0);
        for (Map.Entry<String, Integer> entry : serverTasks.entrySet()) {
            serverTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
}
