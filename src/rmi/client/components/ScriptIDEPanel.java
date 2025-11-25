package rmi.client.components;

import javax.swing.*;
import java.awt.*;

public class ScriptIDEPanel extends JPanel {
    private final JTextArea codeArea;
    private final JButton runBtn;
    private final JComboBox<String> algoCombo;
    private final JTextField inputField;

    public ScriptIDEPanel() {
        setLayout(new BorderLayout());

        // Top panel: algorithm + input
        JPanel topPanel = new JPanel();
        algoCombo = new JComboBox<>(new String[]{"Fibonacci", "Factorial"});
        inputField = new JTextField(5);
        topPanel.add(new JLabel("Algorithm:"));
        topPanel.add(algoCombo);
        topPanel.add(new JLabel("Input:"));
        topPanel.add(inputField);

        // Code area
        codeArea = new JTextArea();

        // Run button
        runBtn = new JButton("Run Script");

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(codeArea), BorderLayout.CENTER);
        add(runBtn, BorderLayout.SOUTH);
    }

    public String getCode() { return codeArea.getText(); }
    public JButton getRunButton() { return runBtn; }
    public JComboBox<String> getAlgoCombo() { return algoCombo; }
    public JTextField getInputField() { return inputField; }
}
