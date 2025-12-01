package rmi.model;

import java.io.Serializable;
import java.util.Date;

public class SubmissionResult implements Serializable {
    private String submissionId;
    private String problemId;
    private String userId;
    private boolean passed;
    private int passedTests;
    private int totalTests;
    private String output;
    private long executionTime;
    private Date submittedAt;

    public SubmissionResult(String submissionId, String problemId, String userId) {
        this.submissionId = submissionId;
        this.problemId = problemId;
        this.userId = userId;
        this.submittedAt = new Date();
    }

    // Getters and Setters
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }
    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
}
