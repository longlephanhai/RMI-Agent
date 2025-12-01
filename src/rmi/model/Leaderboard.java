package rmi.model;

import java.io.Serializable;
import java.util.*;

public class Leaderboard implements Serializable {
    private String problemId;  // null nếu là global leaderboard
    private String type;       // "PROBLEM", "GLOBAL", "TEAM"
    private List<LeaderboardEntry> entries;
    private Date lastUpdated;

    public Leaderboard(String problemId, String type) {
        this.problemId = problemId;
        this.type = type;
        this.entries = new ArrayList<>();
        this.lastUpdated = new Date();
    }

    // Getters
    public List<LeaderboardEntry> getEntries() { return entries; }
    public String getProblemId() { return problemId; }
    public String getType() { return type; }
    public Date getLastUpdated() { return lastUpdated; }

    public void addEntry(LeaderboardEntry entry) {
        entries.add(entry);
        // Tự động sort khi thêm entry mới
        Collections.sort(entries);
    }

    public void updateLeaderboard() {
        this.lastUpdated = new Date();
    }
}