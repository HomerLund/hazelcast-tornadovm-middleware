package kpi.diploma.middleware.client.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import kpi.diploma.middleware.client.api.spi.ClusterClientProvider;
import kpi.diploma.middleware.client.hazelcast.connection.HazelcastConnection;
import kpi.diploma.middleware.client.hazelcast.orchestration.compute.HazelcastComputeManager;
import kpi.diploma.middleware.client.hazelcast.orchestration.distribution.HazelcastDataDistributor;
import kpi.diploma.middleware.client.hazelcast.orchestration.manager.HazelcastSystemManager;
import kpi.diploma.middleware.client.orchestration.compute.ClusterComputeManager;
import kpi.diploma.middleware.client.orchestration.distribution.ClusterDataDistributor;
import kpi.diploma.middleware.client.orchestration.manager.ClusterSystemManager;

import java.util.Properties;

public class HazelcastClusterProvider implements ClusterClientProvider {

    @Override
    public void connect(Properties properties) {
        HazelcastConnection.connect(properties);
    }

    @Override
    public ClusterSystemManager createSystemManager() {
        HazelcastInstance instance = HazelcastConnection.getInstance();
        return new HazelcastSystemManager(instance);
    }

    @Override
    public ClusterDataDistributor<?> createDataDistributor() {
        HazelcastInstance instance = HazelcastConnection.getInstance();
        return new HazelcastDataDistributor<>(instance);
    }

    @Override
    public ClusterComputeManager createComputeManager() {
        HazelcastInstance instance = HazelcastConnection.getInstance();
        return new HazelcastComputeManager(instance);
    }


    @Override
    public void disconnect() {
        HazelcastConnection.disconnect();
    }
}
