package rmi.server;

import rmi.common.ComputeServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);

            ComputeServer server1 = new ComputeServerImpl("Server 1");
            ComputeServer server2 = new ComputeServerImpl("Server 2");
            ComputeServer server3 = new ComputeServerImpl("Server 3");
            registry.rebind("ComputeServer1", server1);
            registry.rebind("ComputeServer2", server2);
            registry.rebind("ComputeServer3", server3);
            System.out.println("Server are ready!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}