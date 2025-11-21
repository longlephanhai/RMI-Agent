package rmi.client.agents;

import rmi.common.Agent;
import java.io.Serializable;

public class GenericAgent extends Agent implements Serializable {

    private SerializableRunnable task;

    public GenericAgent(SerializableRunnable task) {
        this.task = task;
    }

    @Override
    public void execute() throws Exception {
        if (task != null) task.run();
    }

    public interface SerializableRunnable extends Runnable, Serializable {}
}