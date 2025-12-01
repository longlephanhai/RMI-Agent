// File: rmi/common/CodingProblem.java
package rmi.model;

import java.io.Serializable;
import java.util.*;

public class CodingProblem implements Serializable {
    private String problemId;
    private String title;
    private String description;
    private String[] sampleInput;
    private String[] sampleOutput;
    private String difficulty; // EASY, MEDIUM, HARD
    private Map<String, String> starterCode; // language -> code template
    private Map<String, String> testCases; // input -> expected output

    public CodingProblem(String problemId, String title, String description) {
        this.problemId = problemId;
        this.title = title;
        this.description = description;
        this.starterCode = new HashMap<>();
        this.testCases = new HashMap<>();
        this.difficulty = "MEDIUM";
    }

    // Getters and Setters
    public String getProblemId() { return problemId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public void addStarterCode(String language, String code) {
        starterCode.put(language, code);
    }

    public String getStarterCode(String language) {
        return starterCode.getOrDefault(language, "// Write your code here");
    }

    public void addTestCase(String input, String expectedOutput) {
        testCases.put(input, expectedOutput);
    }

    public Map<String, String> getTestCases() { return testCases; }

    public void setSampleInput(String[] input) { this.sampleInput = input; }
    public void setSampleOutput(String[] output) { this.sampleOutput = output; }
    public String[] getSampleInput() { return sampleInput; }
    public String[] getSampleOutput() { return sampleOutput; }
}