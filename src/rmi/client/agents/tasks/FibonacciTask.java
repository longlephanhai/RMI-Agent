package rmi.client.agents.tasks;

import rmi.client.agents.GenericAgent.SerializableRunnable;
import rmi.common.AgentCallback;

public class FibonacciTask implements SerializableRunnable {
    private final int n;
    private final AgentCallback callback;
    private String agentId;

    public FibonacciTask(int n, AgentCallback callback) {
        this.n = n;
        this.callback = callback;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public void run() {
        int a = 0, b = 1;
        try {
            for (int i = 0; i <= n; i++) {
                int current = a;
                int total = a + b;
                a = b;
                b = total;

                if (callback != null) {
                    int progress = (i * 100) / n;
                    callback.updateProgress(agentId, progress);
                }
                Thread.sleep(200);
            }
            if (callback != null) {
                callback.notifyResult(agentId, a);
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