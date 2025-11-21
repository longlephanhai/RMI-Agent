package rmi.client.agents.tasks;

import rmi.client.agents.GenericAgent.SerializableRunnable;
import rmi.common.AgentCallback;

public class FibonacciTask implements SerializableRunnable {
    private final int n;
    private final AgentCallback callback;

    public FibonacciTask(int n, AgentCallback callback) {
        this.n = n;
        this.callback = callback;
    }

    @Override
    public void run() {
        int a = 0, b = 1;
        try {
            for (int i = 0; i <= n; i++) {
                int total = a + b;
                a = b;
                b = total;
                if (callback != null) callback.updateProgress((i * 100) / n);
                Thread.sleep(200);
            }
            if (callback != null) callback.notifyResult(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}