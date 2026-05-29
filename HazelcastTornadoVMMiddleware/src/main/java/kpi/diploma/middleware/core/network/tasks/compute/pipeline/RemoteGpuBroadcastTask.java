package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.client.api.context.GpuContext;
import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableBiFunction;
import kpi.diploma.middleware.core.function.SerializableTriFunction;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.core.network.MiddlewareConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RemoteGpuBroadcastTask <I, B, O> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String inputKey;
    private final String outputKey;
    private final String broadcastKey;
    private final SerializableTriFunction<I, B, GpuContext, O> lambda;
    private final int parallelism;

    public RemoteGpuBroadcastTask(String inputKey, String outputKey, String broadcastKey, SerializableTriFunction<I, B, GpuContext, O> lambda, int parallelism) {
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.broadcastKey = broadcastKey;
        this.lambda = lambda;
        this.parallelism = parallelism;
    }

    @Override
    public Void call() throws Exception {
        try {
            Queue<I> inQueue = NodeLocalWorkspace.waitForQueue(inputKey);
            BlockingQueue<O> outQueue = NodeLocalWorkspace.getOrCreateBlockingQueue(outputKey, MiddlewareConstants.MAX_CHANNEL_CAPACITY);

            NodeLocalWorkspace.registerProducers(outputKey, parallelism);

            B broadcastContext = NodeLocalWorkspace.get(broadcastKey);
            if (broadcastContext == null){
                throw new IllegalStateException("Broadcast data not found for key: " + broadcastKey);
            }

            GpuContext gpuContext = NodeLocalWorkspace.get(MiddlewareConstants.SYSTEM_ACCELERATOR_CACHE_KEY);
            if (gpuContext == null){
                throw new IllegalStateException("GpuContext not found");
            }

            I item;

            while (true){
                if (inQueue instanceof BlockingQueue){
                    item =((BlockingQueue<I>) inQueue).poll(MiddlewareConstants.MAX_CHANNEL_CAPACITY, TimeUnit.MILLISECONDS);
                }
                else{
                    item = inQueue.poll();
                }

                if (item == null){
                    if (NodeLocalWorkspace.inQueueFinished(inputKey) && inQueue.isEmpty()){
                        NodeLocalWorkspace.remove(inputKey);
                        Logger.info("RemoteGpuBroadcastTask", "Input queue " + inputKey + "is finished and removed from workspace");

                        break;
                    }
                    continue;
                }

                O result = lambda.apply(item, broadcastContext, gpuContext);
                outQueue.put(result);
            }

            return null;
        }
        catch (Exception e) {
            System.err.println("Error in RemoteGpuBroadcastTask: ");
            e.printStackTrace();
            throw e;
        }
        finally {
            NodeLocalWorkspace.notifyProducerFinished(outputKey);
        }
    }
}
