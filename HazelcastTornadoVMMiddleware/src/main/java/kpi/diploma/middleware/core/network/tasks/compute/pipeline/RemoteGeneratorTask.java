package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.network.MiddlewareConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class RemoteGeneratorTask<I, O> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String inputKey;
    private final String outputKey;
    private final SerializableFunction<I, Iterable<O>> generatorLambda;

    public RemoteGeneratorTask(String inputKey, String outputKey, SerializableFunction<I, Iterable<O>> fusedLambda) {
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.generatorLambda = fusedLambda;
    }

    @Override
    public Void call() throws Exception {
        I cachedData = NodeLocalWorkspace.get(inputKey);

        if (cachedData == null){
            throw new IllegalStateException("Cache data not found for key: " + inputKey);
        }

        BlockingQueue<O> outQueue = NodeLocalWorkspace.getOrCreateBlockingQueue(outputKey, MiddlewareConstants.MAX_BATCH_SIZE);

        Iterable<O> stream = generatorLambda.apply(cachedData);

        for (O item : stream){
            outQueue.put(item);
        }

        NodeLocalWorkspace.setEndOfStream(true);

        return null;
    }
}
