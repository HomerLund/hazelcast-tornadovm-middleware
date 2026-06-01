package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.client.api.context.GpuContext;
import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.function.SerializableTriFunction;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.core.network.MiddlewareConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RemoteSupplyTask<O> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String outputKey;
    private final SerializableFunction<GpuContext, O> lambda;

    public RemoteSupplyTask(String outputKey, SerializableFunction<GpuContext, O> lambda) {
        this.outputKey = outputKey;
        this.lambda = lambda;
    }

    @Override
    public Void call() throws Exception {
        try {
            BlockingQueue<O> outQueue = NodeLocalWorkspace.getOrCreateBlockingQueue(outputKey, MiddlewareConstants.MAX_CHANNEL_CAPACITY);
            NodeLocalWorkspace.registerProducers(outputKey, 1);

            GpuContext gpuContext = NodeLocalWorkspace.get(MiddlewareConstants.SYSTEM_ACCELERATOR_CACHE_KEY);
            if (gpuContext == null){
                throw new IllegalStateException("GpuContext not found");
            }

            O result = lambda.apply(gpuContext);
            outQueue.put(result);

            return null;
        }
        catch (Exception e) {
            System.err.println("Error in RemoteSupplyTask: ");
            e.printStackTrace();
            throw e;
        }
        finally {
            NodeLocalWorkspace.notifyProducerFinished(outputKey);
        }
    }
}
