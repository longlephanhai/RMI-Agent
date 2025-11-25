package rmi.common;

import java.io.Serializable;

public abstract class Agent implements Serializable {
    protected transient AgentCallback callback;
    protected String agentId;
    protected String clientId; // THÊM FIELD NÀY

    public void setCallback(AgentCallback callback) {
        this.callback = callback;
    }

    public AgentCallback getCallback() {
        return callback;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentId() {
        return agentId;
    }

    // THÊM METHOD NÀY
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    // THÊM METHOD NÀY
    public String getClientId() {
        return clientId;
    }

    public abstract void execute() throws Exception;
}