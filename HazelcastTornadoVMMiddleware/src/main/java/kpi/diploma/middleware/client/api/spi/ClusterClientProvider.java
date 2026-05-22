package kpi.diploma.middleware.client.api.spi;

import kpi.diploma.middleware.client.orchestration.distribution.ClusterDataDistributor;
import kpi.diploma.middleware.client.orchestration.manager.ClusterSystemManager;

import java.util.Properties;

public interface ClusterClientProvider {
    void connect(Properties properties);

    ClusterSystemManager createSystemManager();
    ClusterDataDistributor<?> createDataDistributor();

    void disconnect();
}
