package kpi.diploma.middleware.client.hazelcast.orchestration.pipeline.compute;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import kpi.diploma.middleware.client.orchestration.pipeline.compute.ClusterComputeManager;
import kpi.diploma.middleware.client.orchestration.pipeline.jobs.ComputeJob;
import kpi.diploma.middleware.core.logging.Logger;

import java.util.*;
import java.util.concurrent.Future;

public class HazelcastComputeManager implements ClusterComputeManager {
    private final HazelcastInstance hazelcastInstance;

    public HazelcastComputeManager(HazelcastInstance instance) {
        hazelcastInstance = instance;
    }

    @Override
    public void executePipeline(List<ComputeJob<?>> pipeline) {
        Objects.requireNonNull(pipeline, "Pipeline can not be null");
        Logger.info("ComputeManager", "Deploying pipeline with " + pipeline.size() + " stages");

        List<Future<?>> allFutures = new ArrayList<>();

        try {
            for (ComputeJob<?> job : pipeline) {
                Objects.requireNonNull(job.getNetworkTask(), "Network task can not be null");
                Objects.requireNonNull(job.getTargetPoolName(), "Target pool name can not be null");

                IExecutorService executorService = hazelcastInstance.getExecutorService(job.getTargetPoolName());
                Logger.info("ComputeManager", "Starting stage in pool: [" + job.getTargetPoolName() + "]");

                Map<Member, ? extends Future<?>> futures = executorService.submitToAllMembers(job.getNetworkTask());

                allFutures.addAll(futures.values());
            }

            Logger.info("ComputeManager", "All pipeline stages deployed. Waiting for execution to complete...");

            for (Future<?> future : allFutures) {
                future.get();
            }

            Logger.success("ComputeManager", "Pipeline execution successfully completed");
        } catch (Exception e) {
            Logger.error("ComputeManager", "Error during pipeline execution: " + e.getMessage());
            throw new RuntimeException("Pipeline failed", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Map<String, R> executeAndGatherResults(List<ComputeJob<?>> pipeline) {
        Objects.requireNonNull(pipeline, "Pipeline can not be null");
        if (pipeline.isEmpty()) {
            throw new IllegalArgumentException("Pipeline is empty");
        }

        Logger.info("ComputeManager", "Deploying pipeline with" + pipeline.size() + " stages");

        List<Future<?>> allFutures = new ArrayList<>();
        Map<Member, Future<R>> terminalFutures = null;

        Map<String, R> stringResults = new HashMap<>();

        try {

            for (int i = 0; i < pipeline.size(); i++) {
                ComputeJob<?> job = pipeline.get(i);

                Objects.requireNonNull(job.getNetworkTask(), "Network task can not be null");
                Objects.requireNonNull(job.getTargetPoolName(), "Target pool name can not be null");

                IExecutorService executorService = hazelcastInstance.getExecutorService(job.getTargetPoolName());

                if (i == pipeline.size() - 1) {
                    terminalFutures = (Map<Member, Future<R>>) (Map) executorService.submitToAllMembers(job.getNetworkTask());
                } else {
                    Map<Member, ? extends Future<?>> futures = executorService.submitToAllMembers(job.getNetworkTask());
                    allFutures.addAll(futures.values());
                }
            }

            for (Future<?> future : allFutures) {
                future.get();
            }

            for (Map.Entry<Member, Future<R>> entry : terminalFutures.entrySet()) {
                R nodeResult = entry.getValue().get();

                String nodeId = toNodeId(entry.getKey());
                stringResults.put(nodeId, nodeResult);

                Logger.success("ComputeManager", "Received metrics from Node: " + nodeId);
            }
        } catch (Exception e) {
            Logger.error("ComputeManager", "Error gathering results from cluster: " + e.getMessage());
            throw new RuntimeException("Pipeline failed", e);
        }

        return stringResults;
    }

    private String toNodeId(Member member){
        String host = member.getAddress().getHost();
        int port = member.getAddress().getPort();
        return host.replace(".", "-") + "_" + port;
    }
}
