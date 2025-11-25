package rmi.client;

import rmi.common.AgentCallback;
import rmi.common.AgentInfo;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

public class AgentProgressCallback extends UnicastRemoteObject implements AgentCallback {

    private final String agentName;
    private final String serverName;
    private final ClientGUI gui;
    private final String agentId;
    private final String clientId;

    protected AgentProgressCallback(String agentName, String serverName, ClientGUI gui, String agentId) throws RemoteException {
        super();
        this.agentName = agentName;
        this.serverName = serverName;
        this.gui = gui;
        this.agentId = agentId;
        this.clientId = UUID.randomUUID().toString();
    }

    @Override
    public void updateProgress(String agentId, int progress) throws RemoteException {
        if (this.agentId.equals(agentId)) {
            gui.appendLog(serverName, "PROGRESS", agentName + ": " + progress + "%");
        }
    }

    @Override
    public void notifyResult(String agentId, Object result) throws RemoteException {
        if (this.agentId.equals(agentId)) {
            gui.appendLog(serverName, "RESULT", agentName + " = " + result);
            gui.updateAgentCompletion(serverName, agentId);
        }
    }

    @Override
    public void updateServerStatus(String serverName, int runningTasks, List<AgentInfo> activeAgents) throws RemoteException {
        // KHÔNG LÀM GÌ CẢ - để global callback xử lý server status
        // System.out.println("AgentProgressCallback: Ignoring server status update");
    }

    @Override
    public String getClientId() throws RemoteException {
        return clientId;
    }
}