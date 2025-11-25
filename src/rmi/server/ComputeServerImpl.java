package rmi.server;

import rmi.common.Agent;
import rmi.common.AgentCallback;
import rmi.common.AgentInfo;
import rmi.common.ComputeServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ComputeServerImpl extends UnicastRemoteObject implements ComputeServer {

    private AtomicInteger runningTasks = new AtomicInteger(0);
    private final String serverName;
    private final Map<String, AgentInfo> activeAgents = new ConcurrentHashMap<>();
    private final Set<AgentCallback> registeredClients = Collections.synchronizedSet(new HashSet<>());

    protected ComputeServerImpl(String serverName) throws RemoteException {
        super();
        this.serverName = serverName;
    }

    @Override
    public void submitAgent(Agent agent) throws RemoteException {
        runningTasks.incrementAndGet();

        String agentId = agent.getAgentId();
        String agentType = agent.getClass().getSimpleName();

        // SỬA: Lấy clientId trực tiếp từ agent nếu có method getClientId()
        String clientId = "unknown";

        try {
            // Thử gọi method getClientId() nếu có
            java.lang.reflect.Method getClientIdMethod = agent.getClass().getMethod("getClientId");
            if (getClientIdMethod != null) {
                clientId = (String) getClientIdMethod.invoke(agent);
            }
        } catch (NoSuchMethodException e) {
            // Agent không có method getClientId, thử từ callback
            if (agent.getCallback() != null) {
                try {
                    clientId = agent.getCallback().getClientId();
                } catch (RemoteException ex) {
                    System.err.println("Cannot get client ID from callback: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting client ID: " + e.getMessage());
        }

        System.out.println("=== AGENT INFO ===");
        System.out.println("Agent ID: " + agentId);
        System.out.println("Client ID: " + clientId);
        System.out.println("Agent Type: " + agentType);

        // Tạo agent info
        AgentInfo agentInfo = new AgentInfo(agentId, agentType, clientId, 0, "RUNNING");
        activeAgents.put(agentId, agentInfo);

        // Thông báo cho tất cả client
        notifyAllClients();

        String finalClientId = clientId;
        new Thread(() -> {
            try {
                agent.execute();
                // Khi hoàn thành
                AgentInfo completedInfo = new AgentInfo(agentId, agentType, finalClientId, 100, "COMPLETED");
                activeAgents.put(agentId, completedInfo);
            } catch (Exception ex) {
                ex.printStackTrace();
                AgentInfo errorInfo = new AgentInfo(agentId, agentType, finalClientId, 0, "ERROR");
                activeAgents.put(agentId, errorInfo);
            } finally {
                runningTasks.decrementAndGet();
                notifyAllClients();
            }
        }).start();
    }

    @Override
    public int getRunningTask() throws RemoteException {
        return runningTasks.get();
    }

    @Override
    public List<AgentInfo> getActiveAgents() throws RemoteException {
        return new ArrayList<>(activeAgents.values());
    }

    @Override
    public void registerClient(AgentCallback client) throws RemoteException {
        registeredClients.add(client);
        System.out.println("Client registered: " + client.getClientId());
        // Gửi trạng thái hiện tại cho client mới
        client.updateServerStatus(serverName, runningTasks.get(), getActiveAgents());
    }

    @Override
    public void unregisterClient(AgentCallback client) throws RemoteException {
        registeredClients.remove(client);
        System.out.println("Client unregistered: " + client.getClientId());
    }

    // Trong ComputeServerImpl
    private void notifyAllClients() {
        synchronized (registeredClients) {
            Iterator<AgentCallback> iterator = registeredClients.iterator();
            while (iterator.hasNext()) {
                AgentCallback client = iterator.next();
                try {
                    List<AgentInfo> currentAgents = getActiveAgents();
                    client.updateServerStatus(serverName, runningTasks.get(), currentAgents);
                } catch (RemoteException e) {
                    // Client không còn kết nối, remove khỏi danh sách
                    iterator.remove();
                    System.out.println("Removed disconnected client");
                }
            }
        }
    }

    public void updateAgentProgress(String agentId, int progress) {
        AgentInfo agentInfo = activeAgents.get(agentId);
        if (agentInfo != null) {
            // Tạo agent info mới với progress cập nhật
            AgentInfo updatedInfo = new AgentInfo(
                    agentInfo.getAgentId(),
                    agentInfo.getAgentType(),
                    agentInfo.getClientId(),
                    progress,
                    agentInfo.getStatus()
            );
            activeAgents.put(agentId, updatedInfo);
            notifyAllClients();
        }
    }
}