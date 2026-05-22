package kpi.diploma.middleware.client.orchestration;

import kpi.diploma.middleware.core.data.distribution.DataPartitioner;
import kpi.diploma.middleware.core.data.io.RemoteSourceLoader;
import kpi.diploma.middleware.core.data.io.RemoteTargetWriter;

import java.util.List;

public interface ClusterDataDistributor<T> {
    void distributeData(
        List<T> allItems,
        DataPartitioner<T> partitioner,
        RemoteSourceLoader<T> sourceLoader,
        RemoteTargetWriter<T> targetWriter,
        String workspacePath,
        double[] customProportions
    );
}
