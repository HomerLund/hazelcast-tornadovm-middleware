package kpi.diploma.middleware.server.bootstrap.node.factory.providers;

import com.hazelcast.core.HazelcastInstance;
import kpi.diploma.middleware.server.adapters.cluster.hazelcast.config.HazelcastNodeOptions;
import kpi.diploma.middleware.server.adapters.cluster.hazelcast.config.HazelcastOptionsParser;
import kpi.diploma.middleware.server.adapters.cluster.hazelcast.node.HazelcastWorkerNode;
import kpi.diploma.middleware.server.bootstrap.node.ClusterWorkerNode;
import kpi.diploma.middleware.server.bootstrap.node.factory.NodeProviderStrategy;

import java.util.Properties;

public class HazelcastProviderStrategy implements NodeProviderStrategy {
    @Override
    public ClusterWorkerNode createNode(Properties properties) {
        System.out.println("[Factory] Selected provider: Hazelcast");
        HazelcastNodeOptions options = HazelcastOptionsParser.parse(properties);
        return new HazelcastWorkerNode(options);
    }
}
