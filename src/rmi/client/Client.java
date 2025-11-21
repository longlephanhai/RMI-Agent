package rmi.client;


import rmi.client.agents.GenericAgent;
import rmi.client.agents.tasks.FactorialTask;
import rmi.client.agents.tasks.FibonacciTask;
import rmi.common.Agent;
import rmi.common.AgentCallback;
import rmi.common.ComputeServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
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

            // Cập nhật GUI server ban đầu
            for (String name : serverNames) {
                gui.updateServer(name, 0);
            }

            gui.getSubmitBtn().addActionListener(e -> {
                String algo = (String) gui.getAlgoCombo().getSelectedItem();
                int n;

                try {
                    n = Integer.parseInt(gui.getInputField().getText());
                } catch (NumberFormatException ex) {
                    gui.appendLog("Input phải là số nguyên!");
                    return;
                }

                try {
                    String agentName = algo + " " + n;

                    // Tìm server ít task nhất (load balance)
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

                    // Tạo callback gắn tên agent và server
                    AgentCallback callback = new AgentCallbackImpl(agentName, bestServerName, gui);

                    // Tạo agent theo thuật toán
                    Agent agent;
                    switch (algo) {
                        case "Fibonacci":
                            agent = new GenericAgent(new FibonacciTask(n, callback));
                            break;
                        case "Factorial":
                            agent = new GenericAgent(new FactorialTask(n, callback));
                            break;
                        default:
                            gui.appendLog("Thuật toán không hợp lệ!");
                            return;
                    }
                    agent.setCallback(callback);

                    // Cập nhật GUI: tăng số task trên server
                    gui.updateServer(bestServerName, minTasks + 1);

                    // Gửi agent đến server
                    bestServer.submitAgent(agent);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}