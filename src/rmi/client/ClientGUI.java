package rmi.client;

import rmi.client.components.*;
import rmi.common.AgentInfo;
import rmi.model.CodingProblem;
import rmi.model.Leaderboard;
import rmi.model.SubmissionResult;
import rmi.service.CodingService;

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
    private final ProblemPanel problemPanel;
    private final LeaderboardPanel leaderboardPanel;
    private CodingService codingService;

    // Lưu dữ liệu toàn bộ server
    private final Map<String, Map<String, AgentInfo>> allServerAgents = new HashMap<>();
    private final Map<String, List<String[]>> serverLogs = new HashMap<>();
    private String currentServer = null;

    public ClientGUI() {
        setTitle("RMI Agent Client - Coding Platform");
        setSize(1400, 900); // Tăng size để chứa thêm panel
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        serverPanel = new ServerPanel();
        agentPanel = new AgentPanel();
        logPanel = new LogPanel();
        scriptIDE = new ScriptIDEPanel();
        problemPanel = new ProblemPanel();
        leaderboardPanel = new LeaderboardPanel();

        // Left: server + agent
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, serverPanel, agentPanel);
        leftSplit.setDividerLocation(200);

        // Center: problem + leaderboard
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, problemPanel, leaderboardPanel);
        centerSplit.setDividerLocation(300);

        // Right: log + IDE
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logPanel, scriptIDE);
        rightSplit.setDividerLocation(400);

        // Main layout với 3 cột
        JSplitPane mainLeftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, centerSplit);
        mainLeftSplit.setDividerLocation(400);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainLeftSplit, rightSplit);
        mainSplit.setDividerLocation(800);

        add(mainSplit, BorderLayout.CENTER);

        // Khi chọn server
        serverPanel.setServerSelectionListener(this::selectServer);

        // Khi chọn problem
        problemPanel.setProblemSelectionListener(this::loadProblemToIDE);

        // Hiển thị empty state cho leaderboard
        leaderboardPanel.showEmptyState();

        setVisible(true);
    }

    public LogPanel getLogPanel() { return logPanel; }
    public ScriptIDEPanel getScriptIDE() { return scriptIDE; }
    public ServerPanel getServerPanel() { return serverPanel; }
    public AgentPanel getAgentPanel() { return agentPanel; }
    public ProblemPanel getProblemPanel() { return problemPanel; }
    public LeaderboardPanel getLeaderboardPanel() { return leaderboardPanel; }

    public void setCodingService(CodingService codingService) {
        this.codingService = codingService;
    }

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

    // Khi chọn problem từ problem panel
    private void loadProblemToIDE(CodingProblem problem) {
        SwingUtilities.invokeLater(() -> {
            // Hiển thị problem description
            scriptIDE.appendOutput("=== " + problem.getTitle() + " ===\n");
            scriptIDE.appendOutput("Difficulty: " + problem.getDifficulty() + "\n\n");
            scriptIDE.appendOutput(problem.getDescription() + "\n\n");

            if (problem.getSampleInput() != null && problem.getSampleInput().length > 0) {
                scriptIDE.appendOutput("Sample Input: " + String.join(", ", problem.getSampleInput()) + "\n");
            }
            if (problem.getSampleOutput() != null && problem.getSampleOutput().length > 0) {
                scriptIDE.appendOutput("Sample Output: " + String.join(", ", problem.getSampleOutput()) + "\n");
            }

            // Load starter code tương ứng với ngôn ngữ đang chọn
            String currentLang = (String) scriptIDE.getLanguageCombo().getSelectedItem();
            if (currentLang != null) {
                String langKey = currentLang.toLowerCase().replace("javascript", "js");
                String starterCode = problem.getStarterCode(langKey);
                scriptIDE.getCodeArea().setText(starterCode);
            }

            // Update submit button để submit solution cho problem này
            updateSubmitButtonForProblem(problem);

            // Load leaderboard cho problem này
            loadProblemLeaderboard(problem.getProblemId());

            appendLog("SYSTEM", "INFO", "Loaded problem: " + problem.getTitle());
        });
    }

    private void updateSubmitButtonForProblem(CodingProblem problem) {
        // Remove existing action listeners
        for (var listener : scriptIDE.getSubmitButton().getActionListeners()) {
            scriptIDE.getSubmitButton().removeActionListener(listener);
        }

        // Add new action listener cho problem submission
        scriptIDE.getSubmitButton().addActionListener(e -> submitSolution(problem));
    }

    private void submitSolution(CodingProblem problem) {
        String code = scriptIDE.getCode();
        String language = (String) scriptIDE.getLanguageCombo().getSelectedItem();
        String userId = "user_" + System.currentTimeMillis(); // Tạm thời

        if (code.trim().isEmpty()) {
            scriptIDE.appendOutput("\n❌ Error: Code cannot be empty!\n");
            return;
        }

        try {
            scriptIDE.appendOutput("\n🔄 Submitting solution...\n");

            SubmissionResult result = codingService.submitSolution(
                    problem.getProblemId(), code, language, userId);

            // Hiển thị kết quả
            scriptIDE.appendOutput("\n=== SUBMISSION RESULT ===\n");
            scriptIDE.appendOutput("Status: " + (result.isPassed() ? "✅ PASSED" : "❌ FAILED") + "\n");
            scriptIDE.appendOutput("Tests: " + result.getPassedTests() + "/" + result.getTotalTests() + "\n");
            scriptIDE.appendOutput("Time: " + result.getExecutionTime() + "ms\n");
            scriptIDE.appendOutput("Output: " + result.getOutput() + "\n");

            // Refresh leaderboard
            loadProblemLeaderboard(problem.getProblemId());

            appendLog("SUBMISSION", result.isPassed() ? "SUCCESS" : "FAILED",
                    problem.getTitle() + " - " + result.getPassedTests() + "/" + result.getTotalTests() + " tests passed");

        } catch (Exception ex) {
            scriptIDE.appendOutput("❌ Error: " + ex.getMessage() + "\n");
            appendLog("SUBMISSION", "ERROR", "Submission failed: " + ex.getMessage());
        }
    }

    private void loadProblemLeaderboard(String problemId) {
        try {
            if (codingService != null) {
                Leaderboard leaderboard = codingService.getProblemLeaderboard(problemId);
                leaderboardPanel.updateLeaderboard(leaderboard);
            }
        } catch (Exception ex) {
            leaderboardPanel.showEmptyState();
            appendLog("SYSTEM", "ERROR", "Cannot load leaderboard: " + ex.getMessage());
        }
    }
}