package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.core.network.MiddlewareConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RemoteBatchTask<O> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String inputKey;
    private final String outputKey;
    private final int batchSize;
    private final int parallelism;

    public RemoteBatchTask(String inputKey, String outputKey, int batchSize, int parallelism) {
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.batchSize = batchSize;
        this.parallelism = parallelism;
    }


    @Override
    public Void call() throws Exception {
        try {
            Logger.info("RemoteBatchTask", "Starting");

            Queue<O> inQueue = NodeLocalWorkspace.waitForQueue(inputKey);
            BlockingQueue<List<O>> outQueue = NodeLocalWorkspace.getOrCreateBlockingQueue(outputKey, MiddlewareConstants.MAX_BATCH_QUEUE_CAPACITY);

            NodeLocalWorkspace.registerProducers(outputKey, parallelism);

            List<O> currentBatch = new ArrayList<>(batchSize);
            O item;

            int processed = 0;
            while (true) {
                if (inQueue instanceof BlockingQueue) {
                    item = ((BlockingQueue<O>) inQueue).poll(MiddlewareConstants.MAX_BATCH_QUEUE_CAPACITY, TimeUnit.MILLISECONDS);
                } else {
                    item = inQueue.poll();
                }

                if (item == null) {
                    if (NodeLocalWorkspace.inQueueFinished(inputKey) && inQueue.isEmpty()) {
                        if (!currentBatch.isEmpty()) {
                            outQueue.put(currentBatch);
                        }
                        break;
                    }

                    continue;
                }

                currentBatch.add(item);

                if (currentBatch.size() == batchSize) {
                    outQueue.put(currentBatch);

                    currentBatch = new ArrayList<>(batchSize);
                }

                processed++;
                Logger.info("RemoteBatchTask", "processed " + processed + "/" + inQueue.size());
            }

            Logger.info("RemoteBatchTask", "Ending");
            return null;
        }
        catch (Exception e) {
            System.err.println("Error in RemoteConsumeTask: ");
            e.printStackTrace();
            throw e;
        }
        finally {
            NodeLocalWorkspace.notifyProducerFinished(outputKey);
        }
    }
}
