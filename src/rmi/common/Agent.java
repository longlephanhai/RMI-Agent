package rmi.common;

import java.io.Serializable;

public abstract class Agent implements Serializable {
    protected transient AgentCallback callback;

    public void setCallback(AgentCallback callback) {
        this.callback = callback;
    }

    public abstract void execute() throws Exception;
}