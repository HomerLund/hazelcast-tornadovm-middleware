package kpi.diploma.middleware.client.api.compute;

import kpi.diploma.middleware.client.orchestration.compute.ComputeJob;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.network.tasks.compute.RemoteDiskScanTask;

import java.util.List;

public class ComputeJobBuilder<I, O> {
    public enum SourceStrategy{
        WORKER_DISK,
        CLIENT_RAM
    }

    private SourceStrategy sourceStrategy;

    private String baseDiskDirectory;

    private List<I> clientRamData;

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

    public ComputeJobBuilder<I, O> sourceFromClientRam(List<I> clientRamData){
        this.sourceStrategy = SourceStrategy.CLIENT_RAM;
        this.clientRamData = clientRamData;
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
        if (targetPoolName == null || userLambda == null || cashKey == null){
            throw new IllegalStateException("Incomplete task setup");
        }

        switch (sourceStrategy){
            case WORKER_DISK:
                @SuppressWarnings("unchecked")
                SerializableFunction<String, O> diskLambda = (SerializableFunction<String, O>) userLambda;
                RemoteDiskScanTask<O> diskTask = new RemoteDiskScanTask<>(baseDiskDirectory, diskLambda, cashKey);
                return new ComputeJob.Builder<Void>()
                        .poolName(targetPoolName)
                        .task(diskTask)
                        .build();
            default:
                return new ComputeJob.Builder<Void>()
                        .poolName(targetPoolName)
                        .task(null)
                        .build();
        }
    }
}
