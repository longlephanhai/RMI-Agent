package rmi.client;

import rmi.common.AgentCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AgentCallbackImpl extends UnicastRemoteObject implements AgentCallback {

    private final String agentName;
    private final String serverName;
    private final ClientGUI gui;

    protected AgentCallbackImpl(String agentName, String serverName, ClientGUI gui) throws RemoteException {
        super();
        this.agentName = agentName;
        this.serverName = serverName;
        this.gui = gui;
    }

    @Override
    public void updateProgress(int progress) throws RemoteException {
        gui.appendLog(agentName + " on " + serverName + " Progress: " + progress + "%");
    }

    @Override
    public void notifyResult(Object result) throws RemoteException {
        gui.appendLog(agentName + " Result: " + result);
        int remaining = gui.getServerTasks(serverName) - 1;
        gui.updateServer(serverName, Math.max(0, remaining));
    }
}