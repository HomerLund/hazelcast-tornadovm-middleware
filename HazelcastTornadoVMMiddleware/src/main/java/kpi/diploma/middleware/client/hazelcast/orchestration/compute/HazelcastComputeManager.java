package kpi.diploma.middleware.client.hazelcast.orchestration.compute;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import kpi.diploma.middleware.client.orchestration.compute.ClusterComputeManager;
import kpi.diploma.middleware.client.orchestration.compute.ComputeJob;
import kpi.diploma.middleware.core.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

public class HazelcastComputeManager implements ClusterComputeManager {
    private final HazelcastInstance hazelcastInstance;

    public HazelcastComputeManager(HazelcastInstance instance) {
        hazelcastInstance = instance;
    }

    @Override
    public void executeOnAllNodes(ComputeJob<Void> job) {
        Objects.requireNonNull(job, "Compute job can not be null");
        Objects.requireNonNull(job.getNetworkTask(), "Network task can not be null");
        Objects.requireNonNull(job.getTargetPoolName(), "Target pool name can not be null");

        IExecutorService executorService = hazelcastInstance.getExecutorService(job.getTargetPoolName());
        Logger.info("ComputeManager", "Deploying compute task to pool: [" + job.getTargetPoolName() + "]");

        try{
            Map<Member, Future<Void>> futures = executorService.submitToAllMembers(job.getNetworkTask());

            for (Map.Entry<Member, Future<Void>> entry: futures.entrySet()){
                entry.getValue().get();
                Logger.info("ComputeManager", "Node " + entry.getKey().getUuid() + "Successfully completed the task");
            }
            Logger.info("ComputeManager", "Global cluster execution completed");


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
}
