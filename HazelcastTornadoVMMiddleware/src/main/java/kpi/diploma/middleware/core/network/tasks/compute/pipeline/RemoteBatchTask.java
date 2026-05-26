package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
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

    public RemoteBatchTask(String inputKey, String outputKey, int batchSize) {
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.batchSize = batchSize;
    }


    @Override
    public Void call() throws Exception {
        Queue<O> inQueue = NodeLocalWorkspace.getOrCreateQueue(inputKey);

        BlockingQueue<List<O>> outQueue = NodeLocalWorkspace.getOrCreateBlockingQueue(outputKey, MiddlewareConstants.MAX_BATCH_SIZE);

        List<O> currentBatch = new ArrayList<>(batchSize);
        O item;

        while (true){
            if (inQueue instanceof BlockingQueue){
                item = ((BlockingQueue<O>) inQueue).poll(MiddlewareConstants.MAX_BATCH_SIZE, TimeUnit.MILLISECONDS);
            }
            else{
                item = inQueue.poll();
            }

            if (item == null){
                if (NodeLocalWorkspace.isEndOfStream() && inQueue.isEmpty()){
                    if (!currentBatch.isEmpty()){
                        outQueue.put(currentBatch);
                    }
                    break;
                }

                continue;
            }

            currentBatch.add(item);

            if (currentBatch.size() == batchSize){
                outQueue.put(currentBatch);

                currentBatch = new ArrayList<>(batchSize);
            }
        }

        return null;
    }
}
