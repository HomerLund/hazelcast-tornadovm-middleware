package kpi.diploma.middleware.server.adapters.cluster.hazelcast.node;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import kpi.diploma.middleware.server.adapters.cluster.hazelcast.config.HazelcastNodeOptions;
import kpi.diploma.middleware.server.adapters.cluster.hazelcast.config.HazelcastServerConfig;
import kpi.diploma.middleware.server.bootstrap.node.ClusterWorkerNode;

public class HazelcastWorkerNode implements ClusterWorkerNode {
    private final Config config;
    private HazelcastInstance instance;

    public HazelcastWorkerNode(HazelcastNodeOptions options) {
        this.config = HazelcastServerConfig.createConfig(options);
    }

    @Override
    public void start() {
        System.out.println("[WorkerNode] Starting Hazelcast Node...");
        try {
            this.instance = Hazelcast.newHazelcastInstance(config);

            System.out.println("[WorkerNode] Node successfully started and joined the cluster");
            System.out.println("[WorkerNode] Node UUID: " + instance.getCluster().getLocalMember().getUuid());
            System.out.println("[WorkerNode] Node Address: " + instance.getCluster().getLocalMember().getAddress());
            System.out.println("[WorkerNode] Listening for incoming tasks...");
        }
        catch (Exception e){
            System.err.println("[WorkerNode] Error: Critical error during startup");
            e.printStackTrace();
            throw new RuntimeException("Failed to start Hazelcast Worker Node", e);
        }
    }

    @Override
    public void shutdown() {
        System.out.println("[WorkerNode] Shutdown signal received...");
        if (instance != null && instance.getLifecycleService().isRunning()){
            System.out.println("[WorkerNode] Initiating graceful termination of network connection...");
            instance.shutdown();
            System.out.println("[WorkerNode] Node safely shut down");
        }
        else{
            System.out.println("[WorkerNode] Node is already stopped");
        }
    }
}
