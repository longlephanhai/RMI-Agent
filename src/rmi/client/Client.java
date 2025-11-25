package rmi.client;

import rmi.client.agents.GenericAgent;
import rmi.client.agents.tasks.FactorialTask;
import rmi.client.agents.tasks.FibonacciTask;
import rmi.common.Agent;
import rmi.common.AgentCallback;
import rmi.common.ComputeServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

public class Client {
    private static boolean isSubmitting = false;

    public static void main(String[] args) {
        try {
            // Khởi tạo GUI
            ClientGUI gui = new ClientGUI();
            gui.setVisible(true);

            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ComputeServer server1 = (ComputeServer) registry.lookup("ComputeServer1");
            ComputeServer server2 = (ComputeServer) registry.lookup("ComputeServer2");
            ComputeServer server3 = (ComputeServer) registry.lookup("ComputeServer3");

            String[] serverNames = {"Server 1", "Server 2", "Server 3"};
            ComputeServer[] servers = {server1, server2, server3};

            // Tạo DUY NHẤT 1 global callback
            AgentCallback globalCallback = new AgentCallbackImpl("Global Monitor", "All Servers", gui);

            // Đăng ký với tất cả servers
            for (ComputeServer server : servers) {
                server.registerClient(globalCallback);
            }

            gui.appendLog("SUCCESS", "Connected to servers with Client ID: " + globalCallback.getClientId().substring(0, 8) + "...");


            // Cập nhật GUI server ban đầu
            for (String name : serverNames) {
                gui.updateServer(name, 0);
            }

            gui.getSubmitBtn().addActionListener(e -> {
                if (isSubmitting) {
                    gui.appendLog("Đang xử lý request trước đó...");
                    return;
                }

                isSubmitting = true;
                gui.getSubmitBtn().setEnabled(false);

                try {
                    String algo = (String) gui.getAlgoCombo().getSelectedItem();
                    int n;

                    try {
                        n = Integer.parseInt(gui.getInputField().getText());
                        if (n < 0) {
                            gui.appendLog("ERROR", "Input phải là số nguyên!");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        gui.appendLog("ERROR", "Input phải là số nguyên!");
                        return;
                    }

                    String agentName = algo + " " + n;
                    String agentId = UUID.randomUUID().toString();

                    // Tìm server ít task nhất
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

                    // SỬA: Tạo callback chỉ cho progress/result, KHÔNG cho server status
                    AgentCallback agentCallback = new AgentProgressCallback(agentName, bestServerName, gui, agentId);

                    // Tạo agent theo thuật toán
                    Agent agent;
                    switch (algo) {
                        case "Fibonacci":
                            agent = new GenericAgent(new FibonacciTask(n, agentCallback));
                            break;
                        case "Factorial":
                            agent = new GenericAgent(new FactorialTask(n, agentCallback));
                            break;
                        default:
                            gui.appendLog("Thuật toán không hợp lệ!");
                            return;
                    }

                    agent.setAgentId(agentId);
                    agent.setClientId(globalCallback.getClientId()); // Dùng clientId từ global callback
                    agent.setCallback(agentCallback);

                    // Gửi agent đến server
                    bestServer.submitAgent(agent);

                    gui.appendLog("SUCCESS", "✓ Submitted " + agentName + " to " + bestServerName);
                    gui.appendLog("  Agent ID: " + agentId.substring(0, 8) + "...");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    gui.appendLog("ERROR", "✗ Error: " + ex.getMessage());

                } finally {
                    isSubmitting = false;
                    gui.getSubmitBtn().setEnabled(true);
                }
            });

            // Nút refresh
            gui.getRefreshBtn().addActionListener(e -> {
                try {
                    for (int i = 0; i < servers.length; i++) {
                        int tasks = servers[i].getRunningTask();
                        java.util.List<rmi.common.AgentInfo> agents = servers[i].getActiveAgents();
                        gui.updateServerStatus(serverNames[i], tasks, agents);
                    }
                    gui.appendLog("Manual refresh completed");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // Đóng kết nối khi đóng cửa sổ
            gui.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    try {
                        for (ComputeServer server : servers) {
                            server.unregisterClient(globalCallback);
                        }
                        gui.appendLog("Disconnected from servers");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Cannot connect to servers: " + ex.getMessage(),
                    "Connection Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}