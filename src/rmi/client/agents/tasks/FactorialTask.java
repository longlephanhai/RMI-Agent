package rmi.client.agents.tasks;

import rmi.client.agents.GenericAgent.SerializableRunnable;
import rmi.common.AgentCallback;

public class FactorialTask implements SerializableRunnable {
    private final int n;
    private final AgentCallback callback;

    public FactorialTask(int n, AgentCallback callback) {
        this.n = n;
        this.callback = callback;
    }

    @Override
    public void run() {
        long result = 1;
        try {
            for (int i = 1; i <= n; i++) {
                result *= i;
                if (callback != null) callback.updateProgress((i * 100) / n);
                Thread.sleep(100);
            }
            if (callback != null) callback.notifyResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}