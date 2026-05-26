package kpi.diploma.middleware.client.hazelcast.orchestration.pipeline.cache;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import kpi.diploma.middleware.client.orchestration.pipeline.cache.ClusterCacheManager;
import kpi.diploma.middleware.client.orchestration.pipeline.jobs.CacheJob;
import kpi.diploma.middleware.core.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class HazelcastCacheManager implements ClusterCacheManager {
    private final HazelcastInstance hazelcastInstance;

    public HazelcastCacheManager(HazelcastInstance instance) {
        hazelcastInstance = instance;
    }

    @Override
    public void setupClusterCache(CacheJob job) {
        Objects.requireNonNull(job, "Compute job can not be null");
        Objects.requireNonNull(job.getTargetPoolName(), "Target pool name can not be null");

        IExecutorService executorService = hazelcastInstance.getExecutorService(job.getTargetPoolName());
        Logger.info("CacheManager", "Deploying compute task to pool: [" + job.getTargetPoolName() + "]");

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
                    Logger.info("CacheManager", "Node " + entry.getKey().getAddress() + " Successfully completed the task");
                }
                Logger.info("CacheManager", "Cluster cache successfully initialized");
            }
        }
        catch (Exception e){
            Logger.error("CacheManager", "Error during cache initialization " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
