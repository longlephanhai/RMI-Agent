package rmi.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ComputeServer extends Remote {
    void submitAgent(Agent agent) throws RemoteException;
    int getRunningTask() throws RemoteException;
    List<AgentInfo> getActiveAgents() throws RemoteException;
    void registerClient(AgentCallback client) throws RemoteException;
    void unregisterClient(AgentCallback client) throws RemoteException;
}