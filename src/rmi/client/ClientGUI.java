package rmi.client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import rmi.common.AgentInfo;

public class ClientGUI extends JFrame {
    private JTable serverTable;
    private JTable agentTable;
    private JTable logTable;
    private DefaultTableModel serverTableModel;
    private DefaultTableModel agentTableModel;
    private DefaultTableModel logTableModel;
    private JComboBox<String> algoCombo;
    private JTextField inputField;
    private JButton submitBtn;
    private JButton refreshBtn;
    private JButton clearLogsBtn;
    private Map<String, Integer> serverTasks = new HashMap<>();
    private Map<String, Map<String, AgentInfo>> serverAgents = new HashMap<>();

    public ClientGUI() {
        setTitle("RMI Agent Client - Real-time Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel: input
        JPanel topPanel = new JPanel();
        algoCombo = new JComboBox<>(new String[]{"Fibonacci", "Factorial"});
        inputField = new JTextField(5);
        submitBtn = new JButton("Submit Agent");
        refreshBtn = new JButton("Refresh");
        clearLogsBtn = new JButton("Clear Logs");

        topPanel.add(new JLabel("Algorithm:"));
        topPanel.add(algoCombo);
        topPanel.add(new JLabel("Input:"));
        topPanel.add(inputField);
        topPanel.add(submitBtn);
        topPanel.add(refreshBtn);
        topPanel.add(clearLogsBtn);
        add(topPanel, BorderLayout.NORTH);

        // Center: split pane for agent table and logs
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Agent table (top)
        String[] agentColumns = {"Agent ID", "Type", "Client", "Progress", "Status", "Server"};
        agentTableModel = new DefaultTableModel(agentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        agentTable = new JTable(agentTableModel);
        JScrollPane agentScroll = new JScrollPane(agentTable);
        agentScroll.setPreferredSize(new Dimension(0, 300));

        // Log table (bottom) - THAY THẾ TEXTAREA BẰNG TABLE
        String[] logColumns = {"Time", "Type", "Message"};
        logTableModel = new DefaultTableModel(logColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logTable = new JTable(logTableModel);

        // Đặt chiều rộng cột cho log table
        logTable.getColumnModel().getColumn(0).setPreferredWidth(80); // Time
        logTable.getColumnModel().getColumn(1).setPreferredWidth(80); // Type
        logTable.getColumnModel().getColumn(2).setPreferredWidth(500); // Message

        JScrollPane logScroll = new JScrollPane(logTable);
        logScroll.setPreferredSize(new Dimension(0, 200));

        mainSplitPane.setTopComponent(agentScroll);
        mainSplitPane.setBottomComponent(logScroll);
        mainSplitPane.setDividerLocation(400);
        add(mainSplitPane, BorderLayout.CENTER);

        // Right: server table
        String[] serverColumns = {"Server Name", "Running Tasks"};
        serverTableModel = new DefaultTableModel(serverColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        serverTable = new JTable(serverTableModel);
        JScrollPane tableScroll = new JScrollPane(serverTable);
        tableScroll.setPreferredSize(new Dimension(200, 0));
        add(tableScroll, BorderLayout.EAST);

        // Xử lý nút Clear Logs
        clearLogsBtn.addActionListener(e -> clearLogs());
    }

    public void appendLog(String text) {
        appendLog("INFO", text);
    }

    public void appendLog(String type, String message) {
        SwingUtilities.invokeLater(() -> {
            String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

            // Thêm màu sắc cho các loại log khác nhau
            Color rowColor = Color.WHITE;
            switch (type.toUpperCase()) {
                case "SUCCESS":
                    rowColor = new Color(240, 255, 240); // Xanh nhạt
                    break;
                case "ERROR":
                    rowColor = new Color(255, 240, 240); // Đỏ nhạt
                    break;
                case "PROGRESS":
                    rowColor = new Color(240, 240, 255); // Xanh da trời nhạt
                    break;
                case "RESULT":
                    rowColor = new Color(255, 255, 240); // Vàng nhạt
                    break;
            }

            logTableModel.addRow(new Object[]{time, type, message});

            // Cuộn đến dòng mới nhất
            logTable.scrollRectToVisible(logTable.getCellRect(logTableModel.getRowCount()-1, 0, true));

            // Giới hạn số dòng log (tối đa 1000 dòng)
            if (logTableModel.getRowCount() > 1000) {
                logTableModel.removeRow(0);
            }
        });
    }

    private void clearLogs() {
        SwingUtilities.invokeLater(() -> {
            logTableModel.setRowCount(0);
            appendLog("SYSTEM", "Logs cleared");
        });
    }

    public void updateServer(String name, int runningTasks) {
        serverTasks.put(name, runningTasks);
        updateServerTable();
    }

    public void updateServerStatus(String serverName, int runningTasks, List<AgentInfo> activeAgents) {
        SwingUtilities.invokeLater(() -> {
            serverTasks.put(serverName, runningTasks);

            Map<String, AgentInfo> agentMap = serverAgents.getOrDefault(serverName, new HashMap<>());

            // Cập nhật hoặc thêm mới agent
            for (AgentInfo agent : activeAgents) {
                agentMap.put(agent.getAgentId(), agent);
            }

            // Xóa agent đã hoàn thành khỏi danh sách hiển thị
            Iterator<Map.Entry<String, AgentInfo>> iterator = agentMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, AgentInfo> entry = iterator.next();
                boolean stillActive = activeAgents.stream()
                        .anyMatch(a -> a.getAgentId().equals(entry.getKey()));
                if (!stillActive && "COMPLETED".equals(entry.getValue().getStatus())) {
                    iterator.remove();
                }
            }

            serverAgents.put(serverName, agentMap);
            updateServerTable();
            updateAgentTable();
        });
    }

    private void updateServerTable() {
        SwingUtilities.invokeLater(() -> {
            serverTableModel.setRowCount(0);
            for (Map.Entry<String, Integer> entry : serverTasks.entrySet()) {
                serverTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        });
    }

    private void updateAgentTable() {
        SwingUtilities.invokeLater(() -> {
            agentTableModel.setRowCount(0);

            Set<String> addedAgents = new HashSet<>();

            for (Map.Entry<String, Map<String, AgentInfo>> serverEntry : serverAgents.entrySet()) {
                String serverName = serverEntry.getKey();
                Map<String, AgentInfo> agentMap = serverEntry.getValue();

                for (AgentInfo agent : agentMap.values()) {
                    String agentKey = agent.getAgentId() + "-" + serverName;

                    if (!addedAgents.contains(agentKey)) {
                        agentTableModel.addRow(new Object[]{
                                agent.getAgentId(),
                                agent.getAgentType(),
                                agent.getClientId(),
                                agent.getProgress() + "%",
                                agent.getStatus(),
                                serverName
                        });
                        addedAgents.add(agentKey);
                    }
                }
            }
        });
    }

    public void updateAgentCompletion(String serverName, String agentId) {
        SwingUtilities.invokeLater(() -> {
            Map<String, AgentInfo> agentMap = serverAgents.get(serverName);
            if (agentMap != null) {
                agentMap.remove(agentId);
                updateAgentTable();
            }
            appendLog("SUCCESS", "Agent " + agentId.substring(0, 8) + "... completed on " + serverName);
        });
    }

    public JButton getSubmitBtn() {
        return submitBtn;
    }

    public JButton getRefreshBtn() {
        return refreshBtn;
    }

    public JButton getClearLogsBtn() {
        return clearLogsBtn;
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
}