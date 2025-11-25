package rmi.client;

import rmi.common.AgentCallback;
import rmi.common.AgentInfo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

public class AgentCallbackImpl extends UnicastRemoteObject implements AgentCallback {

    private final String clientId;
    private final ClientGUI gui;

    protected AgentCallbackImpl(String agentName, String serverName, ClientGUI gui) throws RemoteException {
        super();
        this.gui = gui;
        this.clientId = UUID.randomUUID().toString();
    }

    @Override
    public void updateProgress(String agentId, int progress) throws RemoteException {
        // KHÔNG xử lý progress - để AgentProgressCallback xử lý
    }

    @Override
    public void notifyResult(String agentId, Object result) throws RemoteException {
        // KHÔNG xử lý result - để AgentProgressCallback xử lý
    }

    @Override
    public void updateServerStatus(String serverName, int runningTasks, List<AgentInfo> activeAgents) throws RemoteException {
        // CHỈ xử lý server status
        gui.updateServerStatus(serverName, runningTasks, activeAgents);
    }

    @Override
    public String getClientId() throws RemoteException {
        return clientId;
    }
}