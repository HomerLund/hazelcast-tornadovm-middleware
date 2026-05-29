package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableConsumer;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.core.network.MiddlewareConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RemoteConsumeTask<I> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String inputKey;
    private final SerializableConsumer<I> lambda;
    private final int parallelism;

    public RemoteConsumeTask(String inputKey, SerializableConsumer<I> lambda, int parallelism) {
        this.inputKey = inputKey;
        this.lambda = lambda;
        this.parallelism = parallelism;
    }


    @Override
    public Void call() throws Exception {
        try {
            Queue<I> inQueue = NodeLocalWorkspace.waitForQueue(inputKey);

            I item;

            while (true) {
                if (inQueue instanceof BlockingQueue) {
                    item = ((BlockingQueue<I>) inQueue).poll(MiddlewareConstants.MAX_CHANNEL_CAPACITY, TimeUnit.MILLISECONDS);
                } else {
                    item = inQueue.poll();
                }

                if (item == null) {
                    if (NodeLocalWorkspace.inQueueFinished(inputKey) && inQueue.isEmpty()) {
                        NodeLocalWorkspace.remove(inputKey);
                        Logger.info("RemoteConsumeTask", "Input queue " + inputKey + "is finished and removed from workspace");

                        break;
                    }

                    continue;
                }

                lambda.accept(item);
            }

            return null;
        }
        catch (Exception e) {
            System.err.println("Error in RemoteConsumeTask: ");
            e.printStackTrace();
            throw e;
        }
    }
}
