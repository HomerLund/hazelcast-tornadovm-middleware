package kpi.diploma.middleware.server.adapters.cluster.hazelcast.config;

import java.util.*;

public class HazelcastOptionsParser {
    private static final String PREFIX_EXECUTOR = "executor.";

    public static HazelcastNodeOptions parse(Properties properties){
        String clusterName = properties.getProperty("cluster.name", "cluster");

        String portString = properties.getProperty("network.port");
        Integer port = (portString != null && !portString.isBlank())
                ? Integer.parseInt(portString.trim())
                : null;

        String portAutoIncrementString = properties.getProperty("network.port-auto-increment");
        Boolean portAutoIncrement = (portAutoIncrementString != null && !portAutoIncrementString.isBlank())
                ? Boolean.parseBoolean(portAutoIncrementString.trim())
                : null;

        String memberIpsString = properties.getProperty("network.member-ips");
        List<String> memberIps = new ArrayList<>();
        if (memberIpsString != null && !memberIpsString.isBlank()){
            String[] ips = memberIpsString.split(",");
            for (String ip : ips){
                memberIps.add(ip.trim());
            }
        }

        Map<String, Integer> customExecutors = new HashMap<>();

        for (String key : properties.stringPropertyNames()){
            if (key.startsWith(PREFIX_EXECUTOR)){
                String poolName = key.substring(PREFIX_EXECUTOR.length());
                String poolSizeString = properties.getProperty(key);

                try{
                    int poolSize = Integer.parseInt(poolSizeString);
                    customExecutors.put(poolName, poolSize);
                }
                catch (NumberFormatException e){
                    System.err.println("[Parser] Invalid pool size for: " + poolName + ". Excepted a number but got: " + poolSizeString);
                }
            }
        }

        return new HazelcastNodeOptions(clusterName, memberIps, port, portAutoIncrement, customExecutors);
    }
}
