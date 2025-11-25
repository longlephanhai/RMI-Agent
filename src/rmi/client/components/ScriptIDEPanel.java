package rmi.client.components;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ScriptIDEPanel extends JPanel {
    private final RSyntaxTextArea codeArea;
    private final JTextArea outputArea;
    private final JButton runButton;
    private final JButton submitButton;
    private final JComboBox<String> algoCombo;
    private final JTextField inputField;
    private final JComboBox<String> languageCombo;

    // Map ngôn ngữ -> syntax highlight
    private static final Map<String, String> syntaxMap = new HashMap<>();

    static {
        syntaxMap.put("Python", SyntaxConstants.SYNTAX_STYLE_PYTHON);
        syntaxMap.put("JavaScript", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        syntaxMap.put("Ruby", SyntaxConstants.SYNTAX_STYLE_RUBY);
        syntaxMap.put("PHP", SyntaxConstants.SYNTAX_STYLE_PHP);
        syntaxMap.put("Shell", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
        syntaxMap.put("Lua", SyntaxConstants.SYNTAX_STYLE_LUA);
        // Nếu RSyntaxTextArea không có style, sẽ fallback SYNTAX_STYLE_NONE
    }

    public ScriptIDEPanel() {
        setLayout(new BorderLayout());

        // Top panel: algorithm + input + language
        JPanel topPanel = new JPanel();
        algoCombo = new JComboBox<>(new String[]{"Fibonacci", "Factorial"});
        inputField = new JTextField(5);

        // Language combo mở rộng
        languageCombo = new JComboBox<>(syntaxMap.keySet().toArray(new String[0]));

        topPanel.add(new JLabel("Algorithm:"));
        topPanel.add(algoCombo);
        topPanel.add(new JLabel("Input:"));
        topPanel.add(inputField);
        topPanel.add(new JLabel("Language:"));
        topPanel.add(languageCombo);

        // RSyntaxTextArea cho code editor
        codeArea = new RSyntaxTextArea(20, 50);
        codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT); // default
        codeArea.setCodeFoldingEnabled(true);
        RTextScrollPane codeScroll = new RTextScrollPane(codeArea);

        // Output area
        outputArea = new JTextArea(10, 50);
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        // Buttons
        runButton = new JButton("Run Task");
        submitButton = new JButton("Submit Code");
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(runButton);
        bottomPanel.add(submitButton);

        // Split code/output
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeScroll, outputScroll);
        split.setDividerLocation(300);

        add(topPanel, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Thay đổi syntax theo language chọn
        languageCombo.addActionListener(e -> {
            String lang = (String) languageCombo.getSelectedItem();
            setSyntaxStyle(lang);
        });
    }

    // Lấy code từ editor
    public String getCode() { return codeArea.getText(); }

    // Set syntax highlight theo ngôn ngữ
    public void setSyntaxStyle(String language) {
        String style = syntaxMap.getOrDefault(language, SyntaxConstants.SYNTAX_STYLE_NONE);
        codeArea.setSyntaxEditingStyle(style);
    }

    // Xóa và thêm output mới
    public void appendOutput(String text) {
        outputArea.setText(""); // clear trước khi in
        outputArea.append(text + "\n");
    }

    // Getters
    public JButton getRunButton() { return runButton; }
    public JButton getSubmitButton() { return submitButton; }
    public JComboBox<String> getAlgoCombo() { return algoCombo; }
    public JTextField getInputField() { return inputField; }
    public JComboBox<String> getLanguageCombo() { return languageCombo; }
}
