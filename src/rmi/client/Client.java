package rmi.client;

import rmi.client.agents.GenericAgent;
import rmi.client.agents.tasks.FactorialTask;
import rmi.client.agents.tasks.FibonacciTask;
import rmi.common.Agent;
import rmi.common.AgentCallback;
import rmi.common.ComputeServer;
import rmi.common.AgentInfo;

import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {
    private static boolean isSubmitting = false;

    public static void main(String[] args) {
        try {
            ClientGUI gui = new ClientGUI();

            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ComputeServer server1 = (ComputeServer) registry.lookup("ComputeServer1");
            ComputeServer server2 = (ComputeServer) registry.lookup("ComputeServer2");
            ComputeServer server3 = (ComputeServer) registry.lookup("ComputeServer3");

            String[] serverNames = {"Server 1", "Server 2", "Server 3"};
            ComputeServer[] servers = {server1, server2, server3};

            Map<String, Integer> serverTasks = new HashMap<>();
            Map<String, Map<String, AgentInfo>> serverAgents = new HashMap<>();

            AgentCallback globalCallback = new AgentCallbackImpl("Global Monitor", "All Servers", gui);

            for (ComputeServer server : servers) {
                server.registerClient(globalCallback);
            }

            gui.getLogPanel().appendLog("SUCCESS", "Connected to servers with Client ID: "
                    + globalCallback.getClientId().substring(0, 8) + "...");

            for (String name : serverNames) {
                serverTasks.put(name, 0);
            }
            gui.getServerPanel().updateServers(serverTasks);

            // Nút submit / run
            gui.getScriptIDE().getRunButton().addActionListener(e -> {
                if (isSubmitting) {
                    gui.getLogPanel().appendLog("INFO", "Đang xử lý request trước đó...");
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

                    AgentCallback agentCallback = new AgentProgressCallback(algo + " " + n, bestServerName, gui, agentId);

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

            // Refresh manual
            gui.getScriptIDE().getRunButton().addActionListener(e -> {
                try {
                    for (int i = 0; i < servers.length; i++) {
                        int tasks = servers[i].getRunningTask();
                        List<AgentInfo> agents = servers[i].getActiveAgents();

                        serverTasks.put(serverNames[i], tasks);

                        Map<String, AgentInfo> agentMap = new HashMap<>();
                        for (AgentInfo a : agents) agentMap.put(a.getAgentId(), a);
                        serverAgents.put(serverNames[i], agentMap);
                    }
                    gui.getServerPanel().updateServers(serverTasks);
                    gui.getAgentPanel().updateAgents(serverAgents);
                    gui.getLogPanel().appendLog("INFO", "Manual refresh completed");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            gui.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    try {
                        for (ComputeServer server : servers) {
                            server.unregisterClient(globalCallback);
                        }
                        gui.getLogPanel().appendLog("INFO", "Disconnected from servers");
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
