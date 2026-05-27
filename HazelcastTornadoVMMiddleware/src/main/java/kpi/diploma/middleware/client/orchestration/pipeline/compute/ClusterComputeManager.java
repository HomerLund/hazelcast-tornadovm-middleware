package kpi.diploma.middleware.client.orchestration.pipeline.compute;

import com.hazelcast.cluster.Member;
import kpi.diploma.middleware.client.orchestration.pipeline.jobs.ComputeJob;

import java.util.List;
import java.util.Map;

public interface ClusterComputeManager {
    void executePipeline(List<ComputeJob<?>> pipeline);
    <R> Map<String, R> executeAndGatherResults(List<ComputeJob<?>> pipeline);
}
