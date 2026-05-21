package kpi.diploma.middleware.client.hazelcast.connection;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import java.util.Properties;

public class HazelcastConnection {
    private static HazelcastInstance clientInstance;

    private HazelcastConnection(){}

    public static synchronized void connect(Properties properties){
        if (clientInstance == null){
            System.out.println("[Connection] Initializing Hazelcast Client network connection...");

            ClientConfig clientConfig = new ClientConfig();

            String clusterName = properties.getProperty("cluster.name", "cluster");
            clientConfig.setClusterName(clusterName);

            String membersString = properties.getProperty("network.member-ips");
            if (membersString != null && !membersString.isBlank()){
                for (String ip : membersString.split(",")){
                    clientConfig.getNetworkConfig().addAddress(ip.trim());
                }
            }

            clientInstance = HazelcastClient.newHazelcastClient(clientConfig);
            System.out.println("[Connection] Successfully established session with cluster: " + clusterName);

        }
        else{
            System.out.println("[Connection] Connection is already established");
        }
    }

    public static synchronized HazelcastInstance getInstance(){
        if (clientInstance == null){
            throw new IllegalStateException("Error: Cluster connection is not initialized");
        }
        return clientInstance;
    }

    public static synchronized void disconnect(){
        if (clientInstance != null){
            System.out.println("[Connection] Disconnecting client from cluster...");
            clientInstance.shutdown();
            clientInstance = null;
            System.out.println("[Connection] Client session closed cleanly");
        }
    }
}
