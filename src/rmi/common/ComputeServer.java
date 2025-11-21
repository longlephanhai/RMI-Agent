package rmi.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ComputeServer extends Remote {
    void submitAgent(Agent agent) throws RemoteException;
    int getRunningTask() throws  RemoteException;
}