package rmi.common;

import java.io.Serializable;

public class AgentInfo implements Serializable {
    private final String agentId;
    private final String agentType;
    private final String clientId;
    private final int progress;
    private final String status; // RUNNING, COMPLETED, ERROR

    public AgentInfo(String agentId, String agentType, String clientId, int progress, String status) {
        this.agentId = agentId;
        this.agentType = agentType;
        this.clientId = clientId;
        this.progress = progress;
        this.status = status;
    }

    // Getters
    public String getAgentId() { return agentId; }
    public String getAgentType() { return agentType; }
    public String getClientId() { return clientId; }
    public int getProgress() { return progress; }
    public String getStatus() { return status; }
}