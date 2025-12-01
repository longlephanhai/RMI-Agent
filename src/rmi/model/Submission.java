package rmi.model;

import java.io.Serializable;
import java.util.Date;

public class Submission implements Serializable {
    private String submissionId;
    private String problemId;
    private String userId;
    private String code;
    private String language;
    private boolean passed;
    private Date submittedAt;

    // Constructors, getters, setters
    public Submission(String submissionId, String problemId, String userId, String code, String language) {
        this.submissionId = submissionId;
        this.problemId = problemId;
        this.userId = userId;
        this.code = code;
        this.language = language;
        this.submittedAt = new Date();
    }

    // Getters
    public String getSubmissionId() { return submissionId; }
    public String getProblemId() { return problemId; }
    public String getUserId() { return userId; }
    public String getCode() { return code; }
    public String getLanguage() { return language; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public Date getSubmittedAt() { return submittedAt; }
}