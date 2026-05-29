package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.PipelineSink;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.logging.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class RemoteFusedSinkTask<I, O, R> implements Callable<R>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String inputKey;
    private final SerializableFunction<I, O> fusedLambda;
    private final PipelineSink<O, R> sink;
    private final int parallelism;

    public RemoteFusedSinkTask(String inputKey, SerializableFunction<I, O> fusedLambda, PipelineSink<O, R> sink, int parallelism) {
        this.inputKey = inputKey;
        this.fusedLambda = fusedLambda;
        this.sink = sink;
        this.parallelism = parallelism;
    }

    @Override
    public R call() throws Exception {
        try {
            Queue<I> inQueue = NodeLocalWorkspace.waitForQueue(inputKey);
            I item;

            while (true) {
                if (inQueue instanceof BlockingQueue) {
                    item = ((BlockingQueue<I>) inQueue).poll(100, TimeUnit.MILLISECONDS);
                } else {
                    item = inQueue.poll();
                }

                if (item == null) {
                    if (NodeLocalWorkspace.inQueueFinished(inputKey) && inQueue.isEmpty()) {
                        NodeLocalWorkspace.remove(inputKey);
                        Logger.info("RemoteFusedSinkTask", "Input queue " + inputKey + "is finished and removed from workspace");

                        break;
                    }

                    continue;
                }

                O mappedResult = (fusedLambda != null) ? fusedLambda.apply(item) : (O) item;

                sink.process(mappedResult);
            }

            return sink.getResult();
        }
        catch (Exception e) {
            System.err.println("Error in RemoteFusedSinkTask: ");
            e.printStackTrace();
            throw e;
        }
    }

}
