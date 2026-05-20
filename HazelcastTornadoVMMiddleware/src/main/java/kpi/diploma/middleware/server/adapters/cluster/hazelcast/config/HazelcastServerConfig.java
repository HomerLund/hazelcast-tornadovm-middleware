package kpi.diploma.middleware.server.adapters.cluster.hazelcast.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.UserCodeDeploymentConfig;
import kpi.diploma.middleware.core.network.MiddlewareConstants;

import java.util.List;
import java.util.Map;

public class HazelcastServerConfig {
    public static Config createConfig(HazelcastNodeOptions options){
        System.out.println("=== Initializing Hazelcast configuration ===");

        Config config = new Config();

        configureClusterName(config, options.clusterName());
        configureJoin(config, options.memberIps());
        configurePort(config, options.port(), options.portAutoIncrement());
        configureExecutors(config, options.customExecutors());
        configureUserCodeDeployment(config);

        System.out.println("============================================");

        return config;
    }

    private static void configureClusterName(Config config, String clusterName){
        if (clusterName != null && !clusterName.trim().isEmpty()){
            config.setClusterName(clusterName);
            System.out.println("Cluster name: Explicitly set to: " + clusterName);
        }
        else{
            System.out.println("Cluster name: Using default (cluster)");
        }
    }

    private static void configureJoin(Config config, List<String> memberIps){
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();

        if (memberIps == null || memberIps.isEmpty()){
            joinConfig.getMulticastConfig().setEnabled(true);
            joinConfig.getTcpIpConfig().setEnabled(false);

            System.out.println("Network Discovery: Multicast mode enabled (Automatic discovery)");
        }
        else{
            joinConfig.getMulticastConfig().setEnabled(false);
            joinConfig.getTcpIpConfig().setEnabled(true);

            for (String ip : memberIps){
                joinConfig.getTcpIpConfig().addMember(ip);
            }

            System.out.println("Network Discovery: TCP-IP mode enabled | Static members: " + memberIps);
        }
    }

    private static void configurePort(Config config, Integer port, Boolean portAutoIncrement){
        if (port != null){
            config.getNetworkConfig().setPort(port);
            System.out.println("Network: Custom port set to " + port);
        }
        else{
            System.out.println("Network: Using default port (5701)");
        }

        if (portAutoIncrement != null){
            config.getNetworkConfig().setPortAutoIncrement(portAutoIncrement);
            System.out.println("Network: Port Auto-Increment explicitly set to " + portAutoIncrement);
        }
        else{
            System.out.println("Network: Port Auto-Increment using default (true)");
        }
    }

    private static void configureExecutors(Config config, Map<String, Integer> customExecutors){
        ExecutorConfig systemExecutorConfig = config.getExecutorConfig(MiddlewareConstants.SYSTEM_POOL_NAME);
        systemExecutorConfig.setQueueCapacity(5000);

        int defaultPoolSize = Runtime.getRuntime().availableProcessors();
        systemExecutorConfig.setPoolSize(defaultPoolSize);
        System.out.println("System Executor [" + MiddlewareConstants.SYSTEM_POOL_NAME + "]: Pool size set to: " + defaultPoolSize);

        if (customExecutors != null && !customExecutors.isEmpty()){
            System.out.println("--- Registering Custom User Executors --- ");
            for(Map.Entry<String, Integer> entry : customExecutors.entrySet()){
                String poolName = entry.getKey();
                Integer poolSize = entry.getValue();

                ExecutorConfig customExecutorConfig = config.getExecutorConfig(poolName);
                customExecutorConfig.setQueueCapacity(5000);

                if (poolSize != null){
                    customExecutorConfig.setPoolSize(poolSize);
                    System.out.println("Custom Executor [" + poolName + "]: Pool size explicitly set to: " + poolSize);
                }
                else{
                    customExecutorConfig.setPoolSize(defaultPoolSize);
                    System.out.println("Custom Executor [" + poolName + "]: Thread pool size using default (" + defaultPoolSize + "CPU cores)");
                }
            }
            System.out.println("------------------------------------------");
        }
        else{
            System.out.println("No custom user executors defined. Only system pool is active");
        }
    }

    private static void configureUserCodeDeployment(Config config){
        UserCodeDeploymentConfig deploymentConfig = config.getUserCodeDeploymentConfig();
        deploymentConfig.setEnabled(true);
        deploymentConfig.setClassCacheMode(UserCodeDeploymentConfig.ClassCacheMode.ETERNAL);
        deploymentConfig.setProviderMode(UserCodeDeploymentConfig.ProviderMode.LOCAL_AND_CACHED_CLASSES);
        System.out.println("User code deployment: ENABLED | Cache: ETERNAL");
    }
}
