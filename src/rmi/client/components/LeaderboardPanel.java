// File: rmi/client/components/LeaderboardPanel.java
package rmi.client.components;



import rmi.model.Leaderboard;
import rmi.model.LeaderboardEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LeaderboardPanel extends JPanel {
    private final JTable leaderboardTable;
    private final DefaultTableModel tableModel;
    private final JLabel titleLabel;

    public LeaderboardPanel() {
        setLayout(new BorderLayout());

        // Title
        titleLabel = new JLabel("🏆 Leaderboard - Select a Problem");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Rank", "User", "Score", "Time", "Submitted"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };

        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setRowHeight(25);
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Rank
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(120); // User
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Score
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Time
        leaderboardTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Submitted

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        add(scrollPane, BorderLayout.CENTER);

        // Set initial size
        setPreferredSize(new Dimension(400, 300));
    }

    public void updateLeaderboard(Leaderboard leaderboard) {
        SwingUtilities.invokeLater(() -> {
            // Update title
            if (leaderboard.getProblemId() != null) {
                titleLabel.setText("🏆 Leaderboard - " + leaderboard.getProblemId());
            } else {
                titleLabel.setText("🏆 Global Leaderboard");
            }

            // Clear existing data
            tableModel.setRowCount(0);

            // Add entries
            for (LeaderboardEntry entry : leaderboard.getEntries()) {
                String rank = getRankDisplay(entry.getRank());
                String user = entry.getUserName();
                String score = entry.getScore() + " pts";
                String time = entry.getFormattedTime();
                String submitted = new java.text.SimpleDateFormat("MM/dd HH:mm")
                        .format(entry.getSubmittedAt());

                tableModel.addRow(new String[]{rank, user, score, time, submitted});
            }

            // Auto-resize columns to fit content
            for (int column = 0; column < leaderboardTable.getColumnCount(); column++) {
                leaderboardTable.getColumnModel().getColumn(column).setPreferredWidth(
                        leaderboardTable.getColumnModel().getColumn(column).getPreferredWidth()
                );
            }
        });
    }

    private String getRankDisplay(int rank) {
        switch (rank) {
            case 1: return "🥇 1st";
            case 2: return "🥈 2nd";
            case 3: return "🥉 3rd";
            default: return rank + "th";
        }
    }

    public void showEmptyState() {
        SwingUtilities.invokeLater(() -> {
            titleLabel.setText("🏆 Leaderboard");
            tableModel.setRowCount(0);
            tableModel.addRow(new String[]{"-", "No data", "Select a problem", "-", "-"});
        });
    }
}