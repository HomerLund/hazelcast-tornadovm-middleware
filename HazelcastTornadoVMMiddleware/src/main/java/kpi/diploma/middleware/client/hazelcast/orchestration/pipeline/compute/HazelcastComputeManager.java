package kpi.diploma.middleware.client.hazelcast.orchestration.compute;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import kpi.diploma.middleware.client.orchestration.pipeline.compute.ClusterComputeManager;
import kpi.diploma.middleware.client.orchestration.pipeline.jobs.ComputeJob;
import kpi.diploma.middleware.core.logging.Logger;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class HazelcastComputeManager implements ClusterComputeManager {
    private final HazelcastInstance hazelcastInstance;

    public HazelcastComputeManager(HazelcastInstance instance) {
        hazelcastInstance = instance;
    }

    @Override
    public void executeOnAllNodes(ComputeJob<Void> job) {
        Objects.requireNonNull(job, "Compute job can not be null");
        Objects.requireNonNull(job.getTargetPoolName(), "Target pool name can not be null");

        IExecutorService executorService = hazelcastInstance.getExecutorService(job.getTargetPoolName());
        Logger.info("ComputeManager", "Deploying compute task to pool: [" + job.getTargetPoolName() + "]");

        try{
            if (job.getTargetedTaskGenerator() != null){
                List<Member> members = new ArrayList<>(hazelcastInstance.getCluster().getMembers());
                int nodeCount = members.size();

                List<Callable<Void>> targetedTasks = job.getTargetedTaskGenerator().apply(nodeCount);

                List<Future<Void>> futures = new ArrayList<>();
                for (int i = 0; i < nodeCount; i++) {
                    Future<Void> future = executorService.submitToMember(targetedTasks.get(i), members.get(i));
                    futures.add(future);
                }

                for (Future<Void> future : futures){
                    future.get();
                }
            }
            else {
                Objects.requireNonNull(job.getNetworkTask(), "Network task can not be null");

                Map<Member, Future<Void>> futures = executorService.submitToAllMembers(job.getNetworkTask());

                for (Map.Entry<Member, Future<Void>> entry : futures.entrySet()) {
                    entry.getValue().get();
                    Logger.info("ComputeManager", "Node " + entry.getKey().getAddress() + " Successfully completed the task");
                }
                Logger.info("ComputeManager", "Global cluster execution completed");
            }


        }
        catch (Exception e){
            Logger.error("ComputeManager", "Error during cluster computation " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Map<Member, T> executeAndGatherResults(ComputeJob<T> job) {
        Objects.requireNonNull(job, "Compute job can not be null");
        Objects.requireNonNull(job.getNetworkTask(), "Network task can not be null");
        Objects.requireNonNull(job.getTargetPoolName(), "Target pool name can not be null");

        IExecutorService executorService = hazelcastInstance.getExecutorService(job.getTargetPoolName());
        Logger.info("ComputeManager", "Deploying task and waiting for results from pool: [" + job.getTargetPoolName() + "]");

        Map<Member, T> results = new HashMap<>();

        try{
            Map<Member, Future<T>> futures = executorService.submitToAllMembers(job.getNetworkTask());

            for (Map.Entry<Member, Future<T>> entry: futures.entrySet()){
                T nodeResult = entry.getValue().get();
                results.put(entry.getKey(), nodeResult);
                Logger.info("ComputeManager", "Received result from Node " + entry.getKey().getUuid());
            }
            Logger.info("ComputeManager", "Global cluster execution completed");
        }
        catch (Exception e){
            Logger.error("ComputeManager", "Error gathering results from cluster: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return results;
    }

    @Override
    public void executePipeline(List<ComputeJob<?>> pipeline) {
        Objects.requireNonNull(pipeline, "Pipeline can not be null");
        Logger.info("ComputeManager", "Deploying pipeline with " + pipeline.size() + " stages");

        List<Future<?>> allFutures = new ArrayList<>();

        try{
            for (ComputeJob<?> job : pipeline){
                Objects.requireNonNull(job.getNetworkTask(), "Network task can not be null");
                Objects.requireNonNull(job.getTargetPoolName(), "Target pool name can not be null");

                IExecutorService executorService = hazelcastInstance.getExecutorService(job.getTargetPoolName());
                Logger.info("ComputeManager", "Starting stage in pool: [" + job.getTargetPoolName() + "]");

                Map<Member, ? extends Future<?>> futures = executorService.submitToAllMembers(job.getNetworkTask());

                allFutures.addAll(futures.values());
            }

            Logger.info("ComputeManager", "All pipeline stages deployed. Waiting for execution to complete...");

            for (Future<?> future : allFutures){
                future.get();
            }

            Logger.success("ComputeManager", "Pipeline execution successfully completed");
        }
        catch (Exception e){
            Logger.error("ComputeManager", "Error during pipeline execution: " + e.getMessage());
            throw new RuntimeException("Pipeline failed", e);
        }
    }


}
