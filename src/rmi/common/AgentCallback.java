package rmi.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AgentCallback extends Remote {
    void updateProgress(int progress) throws RemoteException;
    void notifyResult(Object result) throws RemoteException;
}