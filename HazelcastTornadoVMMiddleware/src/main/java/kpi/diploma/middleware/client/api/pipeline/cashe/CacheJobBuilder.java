package kpi.diploma.middleware.client.api.pipeline.cashe;

import kpi.diploma.middleware.client.orchestration.pipeline.jobs.CacheJob;
import kpi.diploma.middleware.client.orchestration.pipeline.jobs.ComputeJob;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.network.tasks.compute.cache.RemoteDiskCacheSetupTask;
import kpi.diploma.middleware.core.network.tasks.compute.cache.RemoteRamCacheSetupTask;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class CacheJobBuilder<I, O> {
    public enum SourceStrategy{
        WORKER_DISK,
        CLIENT_RAM
    }

    private SourceStrategy sourceStrategy;

    private String baseDiskDirectory;

    private Function<Integer, List<O>> clientPartitioner;

    private String targetPoolName;
    private SerializableFunction<I, O> userLambda;
    private String cacheKey;

    private CacheJobBuilder(){}

    public static <IN, OUT> CacheJobBuilder<IN, OUT> create() {
        return new CacheJobBuilder<>();
    }

    public CacheJobBuilder<I, O> sourceFromWorkerDisks(String baseDiskDirectory){
        this.sourceStrategy = SourceStrategy.WORKER_DISK;
        this.baseDiskDirectory = baseDiskDirectory;
        return this;
    }

    public CacheJobBuilder<I, O> sourceFromClientRam(Function<Integer, List<O>> clientPartitioner){
        this.sourceStrategy = SourceStrategy.CLIENT_RAM;
        this.clientPartitioner = clientPartitioner;
        return this;
    }

    public CacheJobBuilder<I, O> routeTo(String poolName){
        this.targetPoolName = poolName;
        return this;
    }

    public CacheJobBuilder<I, O> userMethod(SerializableFunction<I, O> userLambda){
        this.userLambda = userLambda;
        return this;
    }

    public CacheJobBuilder<I, O> saveToNodeCache(String cashKey){
        this.cacheKey = cashKey;
        return this;
    }

    public CacheJob buildSetupJob(){
        if (targetPoolName == null || (userLambda == null && clientPartitioner == null) || cacheKey == null){
            throw new IllegalStateException("Incomplete task setup");
        }

        switch (sourceStrategy){
            case WORKER_DISK:
                @SuppressWarnings("unchecked")
                SerializableFunction<String, O> diskLambda = (SerializableFunction<String, O>) userLambda;
                RemoteDiskCacheSetupTask<O> diskTask = new RemoteDiskCacheSetupTask<>(baseDiskDirectory, diskLambda, cacheKey);
                return new CacheJob.Builder()
                        .poolName(targetPoolName)
                        .task(diskTask)
                        .build();
            case CLIENT_RAM:
                Function<Integer, List<Callable<Void>>> taskGenerator = (nodeCount) -> {
                    List<O> userPartitions = clientPartitioner.apply(nodeCount);

                    if (userPartitions.size() != nodeCount){
                        throw new IllegalStateException("The user's lambda returned " + userPartitions.size() + "items, but the expected result was " + nodeCount);
                    }

                    List<Callable<Void>> tasks = new java.util.ArrayList<>();
                    for (O partition : userPartitions){
                        tasks.add(new RemoteRamCacheSetupTask<>(partition, cacheKey));
                    }

                    return tasks;
                };
                return new CacheJob.Builder()
                        .poolName(targetPoolName)
                        .targetedGenerator(taskGenerator)
                        .build();
            default:
                throw new IllegalStateException("Source strategy is not defined. Call sourceFromWorkerDisks() or sourceFromClientRam()");
        }
    }
}
