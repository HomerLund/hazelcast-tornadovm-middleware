package kpi.diploma.middleware.client.orchestration.compute;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class ComputeJob<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String targetPoolName;
    private final Callable<T> networkTask;

    private final transient Function<Integer, List<Callable<T>>> targetedTaskGenerator;

    private ComputeJob(Builder<T> builder){
        this.targetPoolName = builder.targetPoolName;
        this.networkTask = builder.networkTask;
        this.targetedTaskGenerator = builder.targetedTaskGenerator;
    }

    public String getTargetPoolName(){
        return targetPoolName;
    }

    public Callable<T> getNetworkTask(){
        return networkTask;
    }

    public Function<Integer, List<Callable<T>>> getTargetedTaskGenerator() { return targetedTaskGenerator; }

    public static class Builder<T> {
        private String targetPoolName;
        private Callable<T> networkTask;

        private transient Function<Integer, List<Callable<T>>> targetedTaskGenerator;

        public Builder<T> poolName(String targetPoolName){
            this.targetPoolName = targetPoolName;
            return this;
        }

        public Builder<T> task(Callable<T> task){
            this.networkTask = task;
            return this;
        }

        public Builder<T> targetedGenerator(Function<Integer, List<Callable<T>>> generator){
            this.targetedTaskGenerator = generator;
            return this;
        }

        public ComputeJob<T> build(){
            if (targetPoolName == null || (networkTask == null && targetedTaskGenerator == null)){
                throw new IllegalStateException("Cannot build ComputeJob: poolName and networkTask are required");
            }
            else{
                return new ComputeJob<>(this);
            }
        }
    }
}
