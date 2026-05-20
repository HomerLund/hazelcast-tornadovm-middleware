package kpi.diploma.middleware.server.adapters.cluster.hazelcast.config;

import java.util.List;
import java.util.Map;

public record HazelcastNodeOptions(
        String clusterName,
        List<String> memberIps,
        Integer port,
        Boolean portAutoIncrement,
        Map<String, Integer> customExecutors
) {}
