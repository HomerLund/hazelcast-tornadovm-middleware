package kpi.diploma.middleware.client.api.context;

import kpi.diploma.middleware.client.api.spi.ClusterClientProvider;
import kpi.diploma.middleware.client.orchestration.compute.ClusterComputeManager;
import kpi.diploma.middleware.client.orchestration.distribution.ClusterDataDistributor;
import kpi.diploma.middleware.client.orchestration.manager.ClusterSystemManager;
import kpi.diploma.middleware.server.bootstrap.node.config.PropertyFileReader;

import java.util.Objects;
import java.util.Properties;

public class ClusterContext implements AutoCloseable{
    private final ClusterClientProvider provider;
    private final ClusterSystemManager systemManager;
    private final ClusterDataDistributor<?> dataDistributor;
    private final ClusterComputeManager computeManager;


    private ClusterContext(ClusterClientProvider provider, Properties properties){
        this.provider = provider;
        this.provider.connect(properties);

        this.systemManager = provider.createSystemManager();
        this.dataDistributor = provider.createDataDistributor();
        this.computeManager = provider.createComputeManager();
    }

    public static ClusterContext create(ClusterClientProvider provider, String propertiesFilePath){
        Objects.requireNonNull(provider, "ClusterClientProvider can not be null");
        System.out.println("=== Initializing Middleware Cluster Context ===");

        Properties properties = PropertyFileReader.read(propertiesFilePath);

        return new ClusterContext(provider, properties);
    }

    public ClusterSystemManager getSystemManager(){
        return systemManager;
    }

    @SuppressWarnings("unchecked")
    public <T> ClusterDataDistributor<T> getDataDistributor(){
        return  (ClusterDataDistributor<T>) dataDistributor;
    }

    public ClusterComputeManager getComputeManager() { return computeManager; }

    @Override
    public void close(){
        System.out.println("=== Closing Cluster Middleware Context ===");
        if (provider != null) {
            provider.disconnect();
        }
    }
}
