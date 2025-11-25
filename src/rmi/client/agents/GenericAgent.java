package rmi.client.agents;

import rmi.common.Agent;
import java.io.Serializable;
import java.lang.reflect.Method;

public class GenericAgent extends Agent implements Serializable {

    private SerializableRunnable task;

    public GenericAgent(SerializableRunnable task) {
        this.task = task;
    }

    @Override
    public void execute() throws Exception {
        // Truyền agentId cho task nếu task có method setAgentId
        if (task != null && this.getAgentId() != null) {
            try {
                Method setAgentIdMethod = task.getClass().getMethod("setAgentId", String.class);
                setAgentIdMethod.invoke(task, this.getAgentId());
            } catch (NoSuchMethodException e) {
                // Task không có method setAgentId, bỏ qua
            } catch (Exception e) {
                System.err.println("Error setting agentId to task: " + e.getMessage());
            }
        }

        if (task != null) {
            task.run();
        }
    }

    public interface SerializableRunnable extends Runnable, Serializable {}
}