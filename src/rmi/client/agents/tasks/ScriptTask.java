package rmi.client.agents.tasks;

import rmi.client.agents.GenericAgent.SerializableRunnable;
import rmi.common.AgentCallback;

import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ScriptTask implements SerializableRunnable {

    private final String code;
    private final String language;
    private final AgentCallback callback;
    private String agentId;

    // Map ngôn ngữ -> interpreter
    private static final Map<String, String> interpreterMap = new HashMap<>();
    // Map ngôn ngữ -> extension file
    private static final Map<String, String> extensionMap = new HashMap<>();
    // Map ngôn ngữ -> encoding
    private static final Map<String, Charset> encodingMap = new HashMap<>();

    static {
        interpreterMap.put("js", "node");
        interpreterMap.put("py", "python");
        interpreterMap.put("rb", "ruby");
        interpreterMap.put("php", "php");
        interpreterMap.put("sh", "bash");
        interpreterMap.put("lua", "lua");

        extensionMap.put("js", ".js");
        extensionMap.put("py", ".py");
        extensionMap.put("rb", ".rb");
        extensionMap.put("php", ".php");
        extensionMap.put("sh", ".sh");
        extensionMap.put("lua", ".lua");

        // Thiết lập encoding cho từng ngôn ngữ
        encodingMap.put("js", StandardCharsets.UTF_8);
        encodingMap.put("py", StandardCharsets.UTF_8);
        encodingMap.put("rb", StandardCharsets.UTF_8);
        encodingMap.put("php", StandardCharsets.UTF_8);
        encodingMap.put("sh", StandardCharsets.UTF_8);
        encodingMap.put("lua", StandardCharsets.UTF_8);
    }

    public ScriptTask(String code, String language, AgentCallback callback) {
        this.code = code;
        this.language = language.toLowerCase();
        this.callback = callback;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public void run() {
        File temp = null;
        try {
            if (!interpreterMap.containsKey(language)) {
                throw new IllegalArgumentException("Unsupported language: " + language);
            }

            String interpreter = interpreterMap.get(language);
            String extension = extensionMap.get(language);
            Charset encoding = encodingMap.getOrDefault(language, StandardCharsets.UTF_8);

            // Tạo file tạm chứa code với encoding UTF-8
            temp = File.createTempFile("script_", extension);

            // Ghi file với encoding UTF-8
            try (BufferedWriter writer = Files.newBufferedWriter(temp.toPath(), StandardCharsets.UTF_8)) {
                writer.write(code);
            }

            // Khởi tạo process với environment variables hỗ trợ UTF-8
            ProcessBuilder pb = new ProcessBuilder(interpreter, temp.getAbsolutePath());

            // Thiết lập environment variables để hỗ trợ UTF-8
            Map<String, String> env = pb.environment();
            env.put("PYTHONIOENCODING", "utf-8"); // Cho Python
            env.put("NODE_ENCODING", "utf-8");    // Cho Node.js
            env.put("LANG", "en_US.UTF-8");       // Cho Unix/Linux
            env.put("LC_ALL", "en_US.UTF-8");     // Cho Unix/Linux

            pb.redirectErrorStream(true); // merge stdout + stderr
            Process process = pb.start();

            // Đọc output với encoding UTF-8
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder output = new StringBuilder();
            String line;
            int lineCount = 0;

            // Đọc output incremental
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                lineCount++;
                // Gửi progress incremental (giả lập % theo số dòng)
                if (callback != null) {
                    int progress = Math.min(99, lineCount * 5); // max 99%
                    try {
                        callback.updateProgress(agentId, progress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            int exitCode = process.waitFor();

            String result;
            if (exitCode == 0) {
                result = output.toString();
            } else {
                result = "Process exited with code " + exitCode + "\nOutput:\n" + output.toString();
            }

            // Gửi kết quả hoàn tất
            if (callback != null) {
                try {
                    callback.updateProgress(agentId, 100);
                    callback.notifyResult(agentId, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                try {
                    callback.notifyResult(agentId, "Error: " + e.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (temp != null && temp.exists()) {
                try {
                    temp.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}