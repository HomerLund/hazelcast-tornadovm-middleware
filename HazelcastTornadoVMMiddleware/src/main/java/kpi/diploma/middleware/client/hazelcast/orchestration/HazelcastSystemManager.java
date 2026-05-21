package kpi.diploma.middleware.client.hazelcast.orchestration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import kpi.diploma.middleware.client.orchestration.ClusterSystemManager;
import kpi.diploma.middleware.core.network.MiddlewareConstants;
import kpi.diploma.middleware.core.network.tasks.system.RemoteShutdownTask;

public class HazelcastSystemManager implements ClusterSystemManager {
    private final IExecutorService executorService;

    public HazelcastSystemManager(HazelcastInstance instance) {
        this.executorService = instance.getExecutorService(MiddlewareConstants.SYSTEM_POOL_NAME);
    }

    @Override
    public void shutdownAllWorkersNode() {
        System.out.println("[SystemManager] Broadcast: Sending SHUTDOWN command to all worker nodes...");

        try{
            executorService.executeOnAllMembers(new RemoteShutdownTask());
            System.out.println("[SystemManager] Shutdown command broadcasted successfully. Nodes will terminate shortly");
        }
        catch (Exception e){
            System.err.println("[SystemManager] Failed to broadcast shutdown command: " + e.getMessage());
            throw new RuntimeException("Cluster shutdown failed", e);
        }
    }
}
