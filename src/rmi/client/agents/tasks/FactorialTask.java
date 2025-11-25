package rmi.client.agents.tasks;

import rmi.client.agents.GenericAgent.SerializableRunnable;
import rmi.common.AgentCallback;

public class FactorialTask implements SerializableRunnable {
    private final int n;
    private final AgentCallback callback;
    private String agentId;

    public FactorialTask(int n, AgentCallback callback) {
        this.n = n;
        this.callback = callback;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public void run() {
        long result = 1;
        try {
            for (int i = 1; i <= n; i++) {
                result *= i;
                if (callback != null) {
                    int progress = (i * 100) / n;
                    callback.updateProgress(agentId, progress);
                }
                Thread.sleep(100);
            }
            if (callback != null) {
                callback.notifyResult(agentId, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (callback != null) {
                    callback.notifyResult(agentId, "Error: " + e.getMessage());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}