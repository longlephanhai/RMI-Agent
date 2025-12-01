package rmi.model;

import java.io.Serializable;
import java.util.Date;

public class LeaderboardEntry implements Serializable, Comparable<LeaderboardEntry> {
    private String userId;
    private String userName;
    private int score;
    private long executionTime; // milliseconds
    private Date submittedAt;
    private int rank;

    public LeaderboardEntry(String userId, String userName, int score, long executionTime) {
        this.userId = userId;
        this.userName = userName;
        this.score = score;
        this.executionTime = executionTime;
        this.submittedAt = new Date();
    }

    // Sắp xếp: điểm cao nhất -> thời gian nhanh nhất
    @Override
    public int compareTo(LeaderboardEntry other) {
        if (this.score != other.score) {
            return Integer.compare(other.score, this.score); // Điểm cao hơn xếp trên
        }
        return Long.compare(this.executionTime, other.executionTime); // Thời gian ngắn hơn xếp trên
    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getScore() { return score; }
    public long getExecutionTime() { return executionTime; }
    public Date getSubmittedAt() { return submittedAt; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getFormattedTime() {
        return executionTime + "ms";
    }
}