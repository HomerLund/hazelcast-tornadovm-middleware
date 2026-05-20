package kpi.diploma.middleware.server.bootstrap.node.factory;

import kpi.diploma.middleware.server.bootstrap.node.ClusterWorkerNode;
import kpi.diploma.middleware.server.bootstrap.node.factory.providers.HazelcastProviderStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class WorkerNodeFactory {
    private static final Map<String, NodeProviderStrategy> providers = new HashMap<>();

    static {
        providers.put("hazelcast", new HazelcastProviderStrategy());
    }

    public static ClusterWorkerNode createNode(Properties properties){
        String providerName = properties.getProperty("cluster.provider", "hazelcast").toLowerCase();
        NodeProviderStrategy strategy = providers.get(providerName);

        if (strategy == null){
            throw new IllegalArgumentException("Unknown cluster provider: " + providerName);
        }

        return strategy.createNode(properties);
    }
}
