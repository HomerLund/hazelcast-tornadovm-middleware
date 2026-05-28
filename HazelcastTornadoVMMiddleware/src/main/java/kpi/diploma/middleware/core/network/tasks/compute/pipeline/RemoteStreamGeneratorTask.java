package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.logging.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RemoteStreamGeneratorTask<I, O> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String inputKey;
    private final String outputKey;
    private final SerializableFunction<I, Iterable<O>> generatorLambda;

    public RemoteStreamGeneratorTask(String inputKey, String outputKey, SerializableFunction<I, Iterable<O>> fusedLambda) {
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.generatorLambda = fusedLambda;
    }

    @Override
    public Void call() throws Exception {
        try {
            I cachedData = NodeLocalWorkspace.get(inputKey);

            if (cachedData == null) {
                throw new IllegalStateException("Cache data not found for key: " + inputKey);
            }

            Iterable<O> stream = generatorLambda.apply(cachedData);

            ConcurrentLinkedQueue<O> workQueue;
            if (stream instanceof Collection){
                workQueue = new ConcurrentLinkedQueue<>((Collection<O>) stream);
            }
            else{
                workQueue = new ConcurrentLinkedQueue<>();
                for (O item : stream){
                    workQueue.add(item);
                }
            }

            NodeLocalWorkspace.put(outputKey, workQueue);

            NodeLocalWorkspace.setEndOfStream(true);

            Logger.info("Stream Generator", "Successfully materialized stream into Queue under key: " + outputKey);
            return null;
        }
        catch(Exception e){
            System.err.println("Error in RemoteGeneratorTask: ");
            e.printStackTrace();
            throw e;
        }
    }
}
