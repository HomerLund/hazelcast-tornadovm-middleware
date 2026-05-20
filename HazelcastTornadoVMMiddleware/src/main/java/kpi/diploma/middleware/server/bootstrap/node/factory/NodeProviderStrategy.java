package kpi.diploma.middleware.server.bootstrap.node.factory;

import kpi.diploma.middleware.server.bootstrap.node.ClusterWorkerNode;

import java.util.Properties;

public interface NodeProviderStrategy {
    ClusterWorkerNode createNode(Properties properties);
}
