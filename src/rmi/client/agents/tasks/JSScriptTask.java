package rmi.client.agents.tasks;

import rmi.client.agents.GenericAgent.SerializableRunnable;
import rmi.common.AgentCallback;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class JSScriptTask implements SerializableRunnable {
    private final String code;
    private final AgentCallback callback;
    private String agentId;

    public JSScriptTask(String code, AgentCallback callback) {
        this.code = code;
        this.callback = callback;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public void run() {
        try {
            // Tạo command chạy Node.js
            ProcessBuilder pb = new ProcessBuilder("node", "-e", code);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Đọc stdout
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();

            if (callback != null) {
                callback.updateProgress(agentId, 100);
                callback.notifyResult(agentId, output.toString());
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
