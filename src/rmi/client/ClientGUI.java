package rmi.client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ClientGUI extends JFrame {
    private JTextArea logArea;
    private JTable serverTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> algoCombo;
    private JTextField inputField;
    private JButton submitBtn;
    private Map<String, Integer> serverTasks = new HashMap<>();

    public ClientGUI() {
        setTitle("RMI Agent Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel: input
        JPanel topPanel = new JPanel();
        algoCombo = new JComboBox<>(new String[]{"Fibonacci", "Factorial"});
        inputField = new JTextField(5);
        submitBtn = new JButton("Submit Agent");
        topPanel.add(new JLabel("Algorithm:"));
        topPanel.add(algoCombo);
        topPanel.add(new JLabel("Input:"));
        topPanel.add(inputField);
        topPanel.add(submitBtn);
        add(topPanel, BorderLayout.NORTH);

        // Center: log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        add(logScroll, BorderLayout.CENTER);

        // Right: server table
        String[] columns = {"Server Name", "Running Tasks"};
        tableModel = new DefaultTableModel(columns, 0);
        serverTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(serverTable);
        tableScroll.setPreferredSize(new Dimension(200, 0));
        add(tableScroll, BorderLayout.EAST);
    }

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> logArea.append(text + "\n"));
    }

    public void updateServer(String name, int runningTasks) {
        serverTasks.put(name, runningTasks);
        SwingUtilities.invokeLater(() -> {
            boolean found = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(name)) {
                    tableModel.setValueAt(runningTasks, i, 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                tableModel.addRow(new Object[]{name, runningTasks});
            }
        });
    }

    public JButton getSubmitBtn() {
        return submitBtn;
    }

    public JComboBox<String> getAlgoCombo() {
        return algoCombo;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public int getServerTasks(String serverName) {
        return serverTasks.getOrDefault(serverName, 0);
    }

    public void decrementServerTask(String serverName) {
        int tasks = serverTasks.getOrDefault(serverName, 0);
        tasks = Math.max(tasks - 1, 0); // tránh âm
        updateServer(serverName, tasks);
    }
}