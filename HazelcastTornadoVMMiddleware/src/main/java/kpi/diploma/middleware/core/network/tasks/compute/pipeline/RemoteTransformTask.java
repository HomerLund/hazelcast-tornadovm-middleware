package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.network.MiddlewareConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RemoteTransformTask<I, O> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String inputKey;
    private final String outputKey;
    private final SerializableFunction<I, O> lambda;

    public RemoteTransformTask(String inputKey, String outputKey, SerializableFunction lambda) {
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.lambda = lambda;
    }

    @Override
    public Void call() throws Exception {
        Queue<I> inQueue = NodeLocalWorkspace.getOrCreateQueue(inputKey);
        BlockingQueue<O> outQueue = NodeLocalWorkspace.getOrCreateBlockingQueue(outputKey, MiddlewareConstants.MAX_BATCH_SIZE);

        I item;

        while (true){
            if (inQueue instanceof BlockingQueue){
                item = ((BlockingQueue<I>) inQueue).poll(MiddlewareConstants.MAX_BATCH_SIZE, TimeUnit.MILLISECONDS);
            }
            else {
                item = inQueue.poll();
            }

            if (item == null){
                if (NodeLocalWorkspace.isEndOfStream() && inQueue.isEmpty()){
                    break;
                }

                continue;
            }

            O result = lambda.apply(item);
            outQueue.put(result);
        }

        return null;
    }
}
