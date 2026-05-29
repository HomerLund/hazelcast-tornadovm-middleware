package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.logging.Logger;
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
    private final int parallelism;

    public RemoteTransformTask(String inputKey, String outputKey, SerializableFunction lambda, int parallelism) {
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.lambda = lambda;
        this.parallelism = parallelism;
    }

    @Override
    public Void call() throws Exception {
        try {
            Logger.info("RemoteTransformTask", "Starting");

            Queue<I> inQueue = NodeLocalWorkspace.waitForQueue(inputKey);
            BlockingQueue<O> outQueue = NodeLocalWorkspace.getOrCreateBlockingQueue(outputKey, MiddlewareConstants.MAX_CHANNEL_CAPACITY);

            NodeLocalWorkspace.registerProducers(outputKey, parallelism);

            I item;

            int processed = 0;
            while (true) {
                if (inQueue instanceof BlockingQueue) {
                    item = ((BlockingQueue<I>) inQueue).poll(MiddlewareConstants.MAX_CHANNEL_CAPACITY, TimeUnit.MILLISECONDS);
                } else {
                    item = inQueue.poll();
                }

                if (item == null) {
                    if (NodeLocalWorkspace.inQueueFinished(inputKey) && inQueue.isEmpty()) {
                        NodeLocalWorkspace.remove(inputKey);
                        Logger.info("RemoteTransformTask", "Input queue " + inputKey + "is finished and removed from workspace");

                        break;
                    }

                    continue;
                }

                O result = lambda.apply(item);
                outQueue.put(result);

                processed++;
                Logger.info("RemoteTransformTask", "processed " + processed + "/" + inQueue.size());
            }

            Logger.info("RemoteTransformTask", "Ending");
            return null;
        }
        catch (Exception e) {
            System.err.println("Error in RemoteTransformTask: ");
            e.printStackTrace();
            throw e;
        }
        finally {
            NodeLocalWorkspace.notifyProducerFinished(outputKey);
        }
    }

}
