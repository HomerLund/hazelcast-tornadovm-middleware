package kpi.diploma.middleware.client.orchestration.distribution;

import kpi.diploma.middleware.core.data.distribution.DataPartitioner;
import kpi.diploma.middleware.core.data.io.RemoteSourceLoader;
import kpi.diploma.middleware.core.data.io.RemoteTargetWriter;

import java.util.List;

public interface ClusterDataDistributor<T> {
    void distributeData(DistributionJob<T> job);
}
