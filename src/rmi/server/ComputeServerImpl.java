package rmi.server;

import rmi.common.Agent;
import rmi.common.ComputeServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicInteger;

public class ComputeServerImpl extends UnicastRemoteObject implements ComputeServer {

    private AtomicInteger runningTasks = new AtomicInteger(0);
    private final String serverName;

    protected ComputeServerImpl(String serverName) throws RemoteException {
        super();
        this.serverName = serverName;
    }

    @Override
    public void submitAgent(Agent agent) throws RemoteException {
        runningTasks.incrementAndGet();

        System.out.println(serverName + " received an Agent: " + agent.getClass().getSimpleName()
                + ", running tasks: " + runningTasks.get());

        new Thread(() -> {
            try {
                agent.execute();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                runningTasks.decrementAndGet();
            }
        }).start();
    }

    @Override
    public int getRunningTask() throws RemoteException {
        return runningTasks.get();
    }

}