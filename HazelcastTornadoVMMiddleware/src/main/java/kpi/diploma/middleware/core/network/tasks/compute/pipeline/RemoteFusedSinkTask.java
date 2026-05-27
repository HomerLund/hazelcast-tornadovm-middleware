package kpi.diploma.middleware.core.network.tasks.compute.pipeline;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.PipelineSink;
import kpi.diploma.middleware.core.function.SerializableFunction;

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

    public RemoteFusedSinkTask(String inputKey, SerializableFunction<I, O> fusedLambda, PipelineSink<O, R> sink) {
        this.inputKey = inputKey;
        this.fusedLambda = fusedLambda;
        this.sink = sink;
    }

    @Override
    public R call() throws Exception {
        Queue<I> inQueue = NodeLocalWorkspace.getOrCreateQueue(inputKey);
        I item;

        while (true){
            if (inQueue instanceof BlockingQueue){
                item = ((BlockingQueue<I>) inQueue).poll(100, TimeUnit.MILLISECONDS);
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

            O mappedResult = (fusedLambda != null) ? fusedLambda.apply(item) : (O) item;

            sink.process(mappedResult);
        }

        return sink.getResult();
    }
}
