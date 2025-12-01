package rmi.client;

import rmi.client.agents.GenericAgent;
import rmi.client.agents.tasks.FactorialTask;
import rmi.client.agents.tasks.FibonacciTask;
import rmi.client.agents.tasks.ScriptTask;
import rmi.common.Agent;
import rmi.common.AgentCallback;
import rmi.common.ComputeServer;
import rmi.common.AgentInfo;
import rmi.model.CodingProblem;
import rmi.service.CodingService;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {
    private static boolean isSubmitting = false;

    public static void main(String[] args) {
        try {
            ClientGUI gui = new ClientGUI();

            // Registry RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ComputeServer server1 = (ComputeServer) registry.lookup("ComputeServer1");
            ComputeServer server2 = (ComputeServer) registry.lookup("ComputeServer2");
            ComputeServer server3 = (ComputeServer) registry.lookup("ComputeServer3");

            // KẾT NỐI CODING SERVICE
            CodingService codingService = (CodingService) registry.lookup("CodingService");
            gui.setCodingService(codingService); // Thêm method này vào ClientGUI

            // LOAD PROBLEMS KHI KHỞI ĐỘNG
            List<CodingProblem> problems = codingService.getAllProblems();
            gui.getProblemPanel().loadProblems(problems); // Sẽ tạo panel mới

            String[] serverNames = {"Server 1", "Server 2", "Server 3"};
            ComputeServer[] servers = {server1, server2, server3};

            Map<String, Integer> serverTasks = new HashMap<>();
            for (String name : serverNames) serverTasks.put(name, 0);
            gui.getServerPanel().updateServers(serverTasks);

            // Global callback cho dashboard/log
            AgentCallback globalCallback = new AgentCallbackImpl("Global Monitor", "All Servers", gui);
            for (ComputeServer server : servers) server.registerClient(globalCallback);
            gui.getLogPanel().appendLog("SUCCESS", "Connected to servers. Client ID: " + globalCallback.getClientId().substring(0, 8) + "...");

            // Run Task (Fibonacci / Factorial)
            gui.getScriptIDE().getRunButton().addActionListener(e -> {
                if (isSubmitting) {
                    gui.getLogPanel().appendLog("INFO", "Đang xử lý request...");
                    return;
                }
                isSubmitting = true;
                gui.getScriptIDE().getRunButton().setEnabled(false);

                try {
                    String algo = gui.getScriptIDE().getAlgoCombo().getSelectedItem().toString();
                    int n = Integer.parseInt(gui.getScriptIDE().getInputField().getText());
                    if (n < 0) {
                        gui.getLogPanel().appendLog("ERROR", "Input phải là số nguyên!");
                        return;
                    }

                    String agentId = UUID.randomUUID().toString();
                    // Chọn server ít task nhất
                    ComputeServer bestServer = servers[0];
                    String bestServerName = serverNames[0];
                    int minTasks = servers[0].getRunningTask();
                    for (int i = 0; i < servers.length; i++) {
                        int tasks = servers[i].getRunningTask();
                        if (tasks < minTasks) {
                            minTasks = tasks;
                            bestServer = servers[i];
                            bestServerName = serverNames[i];
                        }
                    }

                    AgentCallback agentCallback = new AgentCallbackImpl("Agent", bestServerName, gui) {
                        @Override
                        public void notifyResult(String agentId, Object result) {
                            SwingUtilities.invokeLater(() -> gui.getScriptIDE().appendOutput(result.toString()));
                        }

                        @Override
                        public void updateProgress(String agentId, int progress) {
                        }
                    };

                    Agent agent;
                    switch (algo) {
                        case "Fibonacci":
                            agent = new GenericAgent(new FibonacciTask(n, agentCallback));
                            break;
                        case "Factorial":
                            agent = new GenericAgent(new FactorialTask(n, agentCallback));
                            break;
                        default:
                            gui.getLogPanel().appendLog("ERROR", "Thuật toán không hợp lệ!");
                            return;
                    }

                    agent.setAgentId(agentId);
                    agent.setClientId(globalCallback.getClientId());
                    agent.setCallback(agentCallback);
                    bestServer.submitAgent(agent);

                    gui.getLogPanel().appendLog("SUCCESS", "✓ Submitted " + algo + " " + n + " to " + bestServerName);
                    gui.getLogPanel().appendLog("INFO", "Agent ID: " + agentId.substring(0, 8) + "...");

                } catch (NumberFormatException ex) {
                    gui.getLogPanel().appendLog("ERROR", "Input phải là số nguyên!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    gui.getLogPanel().appendLog("ERROR", "✗ Error: " + ex.getMessage());
                } finally {
                    isSubmitting = false;
                    gui.getScriptIDE().getRunButton().setEnabled(true);
                }
            });

            // Submit Code (đa ngôn ngữ)
            gui.getScriptIDE().getSubmitButton().addActionListener(e -> {
                if (isSubmitting) {
                    gui.getLogPanel().appendLog("INFO", "Đang xử lý request...");
                    return;
                }
                isSubmitting = true;
                gui.getScriptIDE().getSubmitButton().setEnabled(false);

                try {

                    Map<String, String> langMap = Map.of(
                            "Python", "py",
                            "JavaScript", "js",
                            "Ruby", "rb",
                            "PHP", "php",
                            "Shell", "sh",
                            "Lua", "lua"
                    );

                    String code = gui.getScriptIDE().getCode();
                    String guiLang = gui.getScriptIDE().getLanguageCombo().getSelectedItem().toString();
                    String taskLang = langMap.getOrDefault(guiLang, guiLang.toLowerCase());
                    String agentId = UUID.randomUUID().toString();

                    // Chọn server ít task nhất
                    ComputeServer bestServer = servers[0];
                    String bestServerName = serverNames[0];
                    int minTasks = servers[0].getRunningTask();
                    for (int i = 0; i < servers.length; i++) {
                        int tasks = servers[i].getRunningTask();
                        if (tasks < minTasks) {
                            minTasks = tasks;
                            bestServer = servers[i];
                            bestServerName = serverNames[i];
                        }
                    }

                    AgentCallback agentCallback = new AgentCallbackImpl("Agent", bestServerName, gui) {
                        @Override
                        public void notifyResult(String agentId, Object result) {
                            SwingUtilities.invokeLater(() -> gui.getScriptIDE().appendOutput(result.toString()));
                        }

                        @Override
                        public void updateProgress(String agentId, int progress) {
                        }
                    };

                    // Dùng ScriptTask đa ngôn ngữ
                    Agent agent = new GenericAgent(new ScriptTask(code, taskLang, agentCallback));

                    agent.setAgentId(agentId);
                    agent.setClientId(globalCallback.getClientId());
                    agent.setCallback(agentCallback);
                    bestServer.submitAgent(agent);

                    gui.getLogPanel().appendLog("SUCCESS", "✓ Submitted code (" + taskLang + ") to " + bestServerName);
                    gui.getLogPanel().appendLog("INFO", "Agent ID: " + agentId.substring(0, 8) + "...");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    gui.getLogPanel().appendLog("ERROR", "✗ Error: " + ex.getMessage());
                } finally {
                    isSubmitting = false;
                    gui.getScriptIDE().getSubmitButton().setEnabled(true);
                }
            });

            // Unregister global callback khi đóng GUI
            gui.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    try {
                        for (ComputeServer s : servers) s.unregisterClient(globalCallback);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });



        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot connect to servers: " + ex.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
