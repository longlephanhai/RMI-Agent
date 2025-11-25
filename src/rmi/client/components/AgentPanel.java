package rmi.client.components;

import rmi.common.AgentInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class AgentPanel extends JPanel {
    private final DefaultTableModel agentTableModel;

    public AgentPanel() {
        setLayout(new BorderLayout());
        String[] columns = {"Agent ID", "Type", "Client", "Progress", "Status", "Server"};
        agentTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable agentTable = new JTable(agentTableModel);
        add(new JScrollPane(agentTable), BorderLayout.CENTER);
    }

    public void updateAgents(Map<String, Map<String, AgentInfo>> serverAgents) {
        SwingUtilities.invokeLater(() -> {
            agentTableModel.setRowCount(0);
            serverAgents.forEach((server, agents) -> {
                agents.values().forEach(agent -> {
                    agentTableModel.addRow(new Object[]{
                            agent.getAgentId(),
                            agent.getAgentType(),
                            agent.getClientId(),
                            agent.getProgress() + "%",
                            agent.getStatus(),
                            server
                    });
                });
            });
        });
    }

    public void updateAgentsForServer(String serverName, Map<String, Map<String, AgentInfo>> serverAgents) {
        SwingUtilities.invokeLater(() -> {
            agentTableModel.setRowCount(0);

            Map<String, AgentInfo> agents = serverAgents.get(serverName);
            if (agents == null) return;

            agents.values().forEach(agent -> {
                agentTableModel.addRow(new Object[]{
                        agent.getAgentId(),
                        agent.getAgentType(),
                        agent.getClientId(),
                        agent.getProgress() + "%",
                        agent.getStatus(),
                        serverName
                });
            });
        });
    }

}
