package kpi.diploma.middleware.client.api.pipeline.compute;

import kpi.diploma.middleware.client.orchestration.pipeline.jobs.ComputeJob;
import kpi.diploma.middleware.core.function.SerializableConsumer;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.network.tasks.compute.pipeline.RemoteBatchTask;
import kpi.diploma.middleware.core.network.tasks.compute.pipeline.RemoteConsumeTask;
import kpi.diploma.middleware.core.network.tasks.compute.pipeline.RemoteTransformTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataflowJobBuilder<I, O> {
    private final List<ComputeJob<?>> pipelineJobs = new ArrayList<>();

    private String currentInputKey;
    private String currentTargetPoolName;
    private SerializableFunction<Object, Object> fusedLambda = null;

    private DataflowJobBuilder(String startCacheKey){
        this.currentInputKey = startCacheKey;
    }

    public static <IN> DataflowJobBuilder<IN, IN> sourceFromNodeCache(String cashKey){
        return new DataflowJobBuilder<>(cashKey);
    }

    public DataflowJobBuilder<I, O> routeTo(String poolName){
        if (this.currentTargetPoolName != null && fusedLambda != null){
            buildAndAppendTransformTask();
        }

        this.currentTargetPoolName = poolName;
        this.fusedLambda = null;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <NEW_O> DataflowJobBuilder<I, NEW_O> map(SerializableFunction<O, NEW_O> step){
        if(currentTargetPoolName == null){
            throw new IllegalStateException("Call routeTo() before map()");
        }

        if (fusedLambda == null){
            fusedLambda = (SerializableFunction<Object, Object>) step;
        }
        else{
            SerializableFunction<Object, Object> previous = fusedLambda;
            fusedLambda = input -> step.apply((O) previous.apply(input));
        }

        return (DataflowJobBuilder<I, NEW_O>) this;
    }

    @SuppressWarnings("unchecked")
    public DataflowJobBuilder<I, List<O>> asBatch(int batchSize){
        if (currentTargetPoolName == null){
            throw new IllegalStateException("Call routeTo() before asBatch()");
        }

        if (fusedLambda != null){
            buildAndAppendTransformTask();
        }

        String nextChannelKey = "channel_" + UUID.randomUUID().toString().substring(0,8);

        RemoteBatchTask<O> batchTask = new RemoteBatchTask<>(currentInputKey, nextChannelKey, batchSize);

        pipelineJobs.add(new ComputeJob.Builder<Void>()
                .poolName(currentTargetPoolName)
                .task(batchTask)
                .build());

        currentInputKey = nextChannelKey;
        fusedLambda = null;

        return (DataflowJobBuilder<I, List<O>>) this;
    }

    public List<ComputeJob<?>> consume (SerializableConsumer<O> finalLambda){
        if (currentTargetPoolName == null){
            buildAndAppendTransformTask();
        }

        if (fusedLambda != null){
            buildAndAppendTransformTask();
        }

        RemoteConsumeTask<O> consumeTask = new RemoteConsumeTask<>(currentInputKey, finalLambda);

        pipelineJobs.add(new ComputeJob.Builder<Void>()
                .poolName(currentTargetPoolName)
                .task(consumeTask)
                .build());

        return pipelineJobs;
    }

    private void buildAndAppendTransformTask(){
        String nextChannelKey = "channel_" + UUID.randomUUID().toString().substring(0,8);
        RemoteTransformTask<Object, Object> task = new RemoteTransformTask<>(currentInputKey, nextChannelKey, fusedLambda);

        pipelineJobs.add(new ComputeJob.Builder<Void>()
                .poolName(currentTargetPoolName)
                .task(task)
                .build());

        currentInputKey = nextChannelKey;
    }

}
