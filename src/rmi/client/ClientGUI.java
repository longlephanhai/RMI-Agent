package rmi.client;

import rmi.client.components.*;
import rmi.common.AgentInfo;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClientGUI extends JFrame {

    private final ServerPanel serverPanel;
    private final AgentPanel agentPanel;
    private final LogPanel logPanel;
    private final ScriptIDEPanel scriptIDE;

    // Lưu dữ liệu toàn bộ server
    private final Map<String, Map<String, AgentInfo>> allServerAgents = new HashMap<>();
    private final Map<String, List<String[]>> serverLogs = new HashMap<>();
    private String currentServer = null;

    public ClientGUI() {
        setTitle("RMI Agent Client");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        serverPanel = new ServerPanel();
        agentPanel = new AgentPanel();
        logPanel = new LogPanel();
        scriptIDE = new ScriptIDEPanel();

        // Left: server + agent
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, serverPanel, agentPanel);
        leftSplit.setDividerLocation(250);

        // Right: log + IDE
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logPanel, scriptIDE);
        rightSplit.setDividerLocation(400);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, rightSplit);
        mainSplit.setDividerLocation(400);
        add(mainSplit, BorderLayout.CENTER);

        // Khi chọn server
        serverPanel.setServerSelectionListener(this::selectServer);

        setVisible(true);
    }

    public LogPanel getLogPanel() { return logPanel; }
    public ScriptIDEPanel getScriptIDE() { return scriptIDE; }
    public ServerPanel getServerPanel() { return serverPanel; }
    public AgentPanel getAgentPanel() { return agentPanel; }

    // Khi có log mới từ server
    public void appendLog(String serverName, String type, String message) {
        serverLogs.putIfAbsent(serverName, new ArrayList<>());
        serverLogs.get(serverName).add(new String[]{type, message});

        // Nếu server hiện tại được chọn, hiển thị ngay
        if (serverName.equals(currentServer)) {
            SwingUtilities.invokeLater(() -> logPanel.appendLog(type, message));
        }
    }

    // Khi click chọn server
    public void selectServer(String serverName) {
        currentServer = serverName;

        // Xóa log hiện tại
        logPanel.clearLogs();

        // Hiển thị log của server được chọn
        List<String[]> logs = serverLogs.getOrDefault(serverName, List.of());
        for (String[] log : logs) {
            logPanel.appendLog(log[0], log[1]);
        }

        // Cập nhật agent table cho server
        agentPanel.updateAgentsForServer(serverName, allServerAgents);
    }

    // Cập nhật trạng thái server: số task + danh sách agent
    public void updateServerStatus(String serverName, int runningTasks, List<AgentInfo> activeAgents) {
        serverPanel.updateServers(Map.of(serverName, runningTasks));

        Map<String, AgentInfo> agents = activeAgents.stream()
                .collect(Collectors.toMap(AgentInfo::getAgentId, a -> a));
        allServerAgents.put(serverName, agents);

        if (serverName.equals(currentServer)) {
            agentPanel.updateAgentsForServer(serverName, allServerAgents);
        }
    }

    // Khi agent hoàn thành
    public void updateAgentCompletion(String serverName, String agentId) {
        appendLog(serverName, "INFO", "Agent " + agentId.substring(0, 8) + " completed.");

        // Cập nhật agent table
        if (serverName.equals(currentServer)) {
            agentPanel.updateAgentsForServer(serverName, allServerAgents);
        }
    }
}
