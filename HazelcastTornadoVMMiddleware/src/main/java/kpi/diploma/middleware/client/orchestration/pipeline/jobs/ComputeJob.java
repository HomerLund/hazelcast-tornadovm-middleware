package kpi.diploma.middleware.client.orchestration.pipeline.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class ComputeJob<T> extends ClusterJob<T> {
    private ComputeJob(Builder<T> builder){
        super(builder.targetPoolName, builder.networkTask);
    }

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
            if (targetPoolName == null || networkTask == null){
                throw new IllegalStateException("Cannot build ComputeJob: poolName and networkTask are required");
            }
            else{
                return new ComputeJob<>(this);
            }
        }
    }
}
