package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableConsumer;
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

    public RemoteConsumeTask(String inputKey, SerializableConsumer<I> lambda) {
        this.inputKey = inputKey;
        this.lambda = lambda;
    }


    @Override
    public Void call() throws Exception {
        Queue<I> inQueue = NodeLocalWorkspace.getOrCreateQueue(inputKey);

        I item;

        while(true){
            if (inQueue instanceof BlockingQueue){
                item = ((BlockingQueue<I>) inQueue).poll(MiddlewareConstants.MAX_CHANNEL_CAPACITY, TimeUnit.MILLISECONDS);
            }
            else{
                item = inQueue.poll();
            }

            if (item == null){
                if (NodeLocalWorkspace.isEndOfStream() && inQueue.isEmpty()){
                    break;
                }

                continue;
            }

            lambda.accept(item);
        }

        return null;
    }
}
