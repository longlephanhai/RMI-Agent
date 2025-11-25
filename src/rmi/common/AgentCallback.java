package rmi.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AgentCallback extends Remote {
    void updateProgress(String agentId, int progress) throws RemoteException;
    void notifyResult(String agentId, Object result) throws RemoteException;
    void updateServerStatus(String serverName, int runningTasks, List<AgentInfo> activeAgents) throws RemoteException;
    String getClientId() throws RemoteException;
}