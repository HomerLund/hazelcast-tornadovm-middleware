package kpi.diploma.middleware.client.api.compute;

import kpi.diploma.middleware.client.orchestration.compute.ComputeJob;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.network.tasks.compute.RemoteDiskCacheSetupTask;
import kpi.diploma.middleware.core.network.tasks.compute.RemoteRamCacheSetupTask;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class ComputeJobBuilder<I, O> {
    public enum SourceStrategy{
        WORKER_DISK,
        CLIENT_RAM
    }

    private SourceStrategy sourceStrategy;

    private String baseDiskDirectory;

    private List<I> clientRamData;
    private Function<Integer, List<O>> clientPartitioner;

    private String targetPoolName;
    private SerializableFunction<I, O> userLambda;
    private String cashKey;

    private ComputeJobBuilder(){}

    public static <IN, OUT> ComputeJobBuilder<IN, OUT> create() {
        return new ComputeJobBuilder<>();
    }

    public ComputeJobBuilder<I, O> sourceFromWorkerDisks(String baseDiskDirectory){
        this.sourceStrategy = SourceStrategy.WORKER_DISK;
        this.baseDiskDirectory = baseDiskDirectory;
        return this;
    }

    public ComputeJobBuilder<I, O> sourceFromClientRam(Function<Integer, List<O>> clientPartitioner){
        this.sourceStrategy = SourceStrategy.CLIENT_RAM;
        this.clientPartitioner = clientPartitioner;
        return this;
    }

    public ComputeJobBuilder<I, O> routeTo(String poolName){
        this.targetPoolName = poolName;
        return this;
    }

    public ComputeJobBuilder<I, O> userMethod(SerializableFunction<I, O> userLambda){
        this.userLambda = userLambda;
        return this;
    }

    public ComputeJobBuilder<I, O> saveToNodeCache(String cashKey){
        this.cashKey = cashKey;
        return this;
    }

    public ComputeJob<Void> buildSetupJob(){
        if (targetPoolName == null || (userLambda == null && clientPartitioner == null) || cashKey == null){
            throw new IllegalStateException("Incomplete task setup");
        }

        switch (sourceStrategy){
            case WORKER_DISK:
                @SuppressWarnings("unchecked")
                SerializableFunction<String, O> diskLambda = (SerializableFunction<String, O>) userLambda;
                RemoteDiskCacheSetupTask<O> diskTask = new RemoteDiskCacheSetupTask<>(baseDiskDirectory, diskLambda, cashKey);
                return new ComputeJob.Builder<Void>()
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
                        tasks.add(new RemoteRamCacheSetupTask<>(partition, cashKey));
                    }

                    return tasks;
                };
                return new ComputeJob.Builder<Void>()
                        .poolName(targetPoolName)
                        .targetedGenerator(taskGenerator)
                        .build();
            default:
                return new ComputeJob.Builder<Void>()
                        .poolName(targetPoolName)
                        .task(null)
                        .build();
        }
    }
}
