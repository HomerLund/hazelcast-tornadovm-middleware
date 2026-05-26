package kpi.diploma.middleware.client.orchestration.compute;

import com.hazelcast.cluster.Member;

import java.util.List;
import java.util.Map;

public interface ClusterComputeManager {
    void executeOnAllNodes(ComputeJob<Void> job);
    <T> Map<Member, T> executeAndGatherResults(ComputeJob<T> job);
    public void executePipeline(List<ComputeJob<?>> pipeline);
}
