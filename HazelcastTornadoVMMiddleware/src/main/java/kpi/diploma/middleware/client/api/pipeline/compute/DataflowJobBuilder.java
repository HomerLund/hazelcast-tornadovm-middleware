package kpi.diploma.middleware.client.api.pipeline.compute;

import kpi.diploma.middleware.client.api.context.GpuContext;
import kpi.diploma.middleware.client.orchestration.pipeline.jobs.ComputeJob;
import kpi.diploma.middleware.core.function.*;
import kpi.diploma.middleware.core.network.tasks.compute.pipeline.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataflowJobBuilder<I, O> {
    private final List<ComputeJob<?>> pipelineJobs = new ArrayList<>();

    private String currentInputKey;
    private String currentTargetPoolName;
    private SerializableFunction<Object, Object> fusedLambda = null;
    private int currentParallelism = 1;

    private DataflowJobBuilder(String startCacheKey){
        this.currentInputKey = startCacheKey;
    }

    public static <IN> DataflowJobBuilder<IN, IN> sourceFromNodeCache(String cashKey){
        return new DataflowJobBuilder<>(cashKey);
    }

    public DataflowJobBuilder<I, O> routeTo(String poolName){
        return routeTo(poolName, 1);
    }

    public DataflowJobBuilder<I, O> routeTo(String poolName, int parallelism){
        if (this.currentTargetPoolName != null && fusedLambda != null){
            buildAndAppendTransformTask();
        }

        this.currentTargetPoolName = poolName;
        this.fusedLambda = null;
        this.currentParallelism = parallelism;
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
    public <B, NEW_O> DataflowJobBuilder<I, NEW_O> mapWithBroadcast(String broadcastKey, Class<B> broadcastType , SerializableBiFunction<O, B, NEW_O> step){
        if (currentTargetPoolName == null){
            throw new IllegalStateException("Call routeTo() before mapWithBroadcast()");
        }

        String nextChannelKey = "channel_" + UUID.randomUUID().toString().substring(0,8);

        RemoteBroadcastTransformTask<O, B, NEW_O> task = new RemoteBroadcastTransformTask<>(currentInputKey, nextChannelKey, broadcastKey, step, currentParallelism);

        pipelineJobs.add(new ComputeJob.Builder<Void>()
                .poolName(currentTargetPoolName)
                .task(task)
                .parallelism(currentParallelism)
                .build());

        currentInputKey = nextChannelKey;

        return (DataflowJobBuilder<I, NEW_O>)  this;
    }

    public <B, NEW_O> DataflowJobBuilder<I, NEW_O> mapWithGpuBroadcast(String broadcastKey, Class<B> broadcastType , SerializableTriFunction<O, B, GpuContext, NEW_O> step){
        if (currentTargetPoolName == null){
            throw new IllegalStateException("Call routeTo() before mapWithGpuBroadcast()");
        }

        String nextChannelKey = "channel_" + UUID.randomUUID().toString().substring(0,8);

        RemoteGpuBroadcastTask<O, B, NEW_O> task = new RemoteGpuBroadcastTask<>(currentInputKey, nextChannelKey, broadcastKey, step, currentParallelism);

        pipelineJobs.add(new ComputeJob.Builder<Void>()
                .poolName(currentTargetPoolName)
                .task(task)
                .parallelism(currentParallelism)
                .build());

        currentInputKey = nextChannelKey;

        return (DataflowJobBuilder<I, NEW_O>)  this;
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

        RemoteBatchTask<O> batchTask = new RemoteBatchTask<>(currentInputKey, nextChannelKey, batchSize, currentParallelism);

        pipelineJobs.add(new ComputeJob.Builder<Void>()
                .poolName(currentTargetPoolName)
                .task(batchTask)
                .parallelism(currentParallelism)
                .build());

        currentInputKey = nextChannelKey;
        fusedLambda = null;

        return (DataflowJobBuilder<I, List<O>>) this;
    }


    public <NEW_O> List<ComputeJob<?>> generateStream(String outputKey, SerializableFunction<O, Iterable<NEW_O>> generator){
        if (currentTargetPoolName == null){
            throw new IllegalStateException("Call routeTo() before generateStream()");
        }

        if (fusedLambda != null){
            buildAndAppendTransformTask();
        }

        RemoteStreamGeneratorTask<O, NEW_O> task = new RemoteStreamGeneratorTask<>(currentInputKey, outputKey, generator);

        pipelineJobs.add(new ComputeJob.Builder<Void>()
                .poolName(currentTargetPoolName)
                .task(task)
                .parallelism(currentParallelism)
                .build());

        return pipelineJobs;
    }

    public List<ComputeJob<?>> consume (SerializableConsumer<O> finalLambda){
        if (currentTargetPoolName == null){
            throw new IllegalStateException("Call routeTo() before consume()");
        }

        if (fusedLambda != null){
            buildAndAppendTransformTask();
        }

        RemoteConsumeTask<O> consumeTask = new RemoteConsumeTask<>(currentInputKey, finalLambda, currentParallelism);

        pipelineJobs.add(new ComputeJob.Builder<Void>()
                .poolName(currentTargetPoolName)
                .task(consumeTask)
                .parallelism(currentParallelism)
                .build());

        return pipelineJobs;
    }

    @SuppressWarnings("unchecked")
    public <R> List<ComputeJob<?>> sink(PipelineSink<O, R> sink){
        if (currentTargetPoolName == null){
            throw new IllegalStateException("Call routeTo() before sink()");
        }

        RemoteFusedSinkTask<Object, O, R> fusedTask = new RemoteFusedSinkTask<>(
                currentInputKey,
                (SerializableFunction<Object, O>) fusedLambda,
                sink,
                currentParallelism
        );

        pipelineJobs.add(new ComputeJob.Builder<R>()
                .poolName(currentTargetPoolName)
                .task(fusedTask)
                .parallelism(currentParallelism)
                .build());

        return pipelineJobs;
    }

    private void buildAndAppendTransformTask(){
        String nextChannelKey = "channel_" + UUID.randomUUID().toString().substring(0,8);
        RemoteTransformTask<Object, Object> task = new RemoteTransformTask<>(currentInputKey, nextChannelKey, fusedLambda, currentParallelism);

        pipelineJobs.add(new ComputeJob.Builder<Void>()
                .poolName(currentTargetPoolName)
                .task(task)
                .parallelism(currentParallelism)
                .build());

        currentInputKey = nextChannelKey;
    }

}
