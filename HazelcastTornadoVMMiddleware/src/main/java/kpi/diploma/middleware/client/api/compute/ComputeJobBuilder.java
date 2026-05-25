package kpi.diploma.middleware.client.api.compute;

import kpi.diploma.middleware.client.orchestration.compute.ComputeJob;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.network.tasks.compute.RemoteSetupTask;

import java.util.function.Function;

public class ComputeJobBuilder<I, O> {
    private String targetPoolName;
    private SerializableFunction<I, O> userLambda;
    private String cashKey;

    private ComputeJobBuilder(){}

    public static <IN, OUT> ComputeJobBuilder<IN, OUT> create() {
        return new ComputeJobBuilder<>();
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

        @SuppressWarnings("unchecked")
        RemoteSetupTask<O> networkTask = new RemoteSetupTask<>((SerializableFunction<Void, O>) userLambda, cashKey);

        return new ComputeJob.Builder<Void>()
                .poolName(targetPoolName)
                .task(networkTask)
                .build();
    }
}
