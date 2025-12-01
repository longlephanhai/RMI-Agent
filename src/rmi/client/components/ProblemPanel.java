// File: rmi/client/components/ProblemPanel.java
package rmi.client.components;


import rmi.model.CodingProblem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProblemPanel extends JPanel {
    private final DefaultTableModel problemTableModel;
    private final JTable problemTable;
    private List<CodingProblem> problems;
    private ProblemSelectionListener listener;

    public interface ProblemSelectionListener {
        void problemSelected(CodingProblem problem);
    }

    public ProblemPanel() {
        setLayout(new BorderLayout());

        // Table hiển thị danh sách problems
        String[] columns = {"ID", "Title", "Difficulty", "Status"};
        problemTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        problemTable = new JTable(problemTableModel);
        problemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Khi chọn problem
        problemTable.getSelectionModel().addListSelectionListener(e -> {
            int row = problemTable.getSelectedRow();
            if (row >= 0 && !e.getValueIsAdjusting() && listener != null) {
                String problemId = (String) problemTableModel.getValueAt(row, 0);
                CodingProblem selected = problems.stream()
                        .filter(p -> p.getProblemId().equals(problemId))
                        .findFirst().orElse(null);
                if (selected != null) listener.problemSelected(selected);
            }
        });

        add(new JLabel("📝 Coding Problems"), BorderLayout.NORTH);
        add(new JScrollPane(problemTable), BorderLayout.CENTER);
    }

    public void setProblemSelectionListener(ProblemSelectionListener listener) {
        this.listener = listener;
    }

    public void loadProblems(List<CodingProblem> problems) {
        this.problems = problems;
        SwingUtilities.invokeLater(() -> {
            problemTableModel.setRowCount(0);
            for (CodingProblem problem : problems) {
                problemTableModel.addRow(new Object[]{
                        problem.getProblemId(),
                        problem.getTitle(),
                        problem.getDifficulty(),
                        "Not attempted" // TODO: Update based on user submissions
                });
            }
        });
    }
}