package kpi.diploma.middleware.client.orchestration.pipeline.jobs;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class CacheJob extends ClusterJob<Void> {
    private final transient Function<Integer, List<Callable<Void>>> targetedTaskGenerator;

    private CacheJob(Builder builder){
        super(builder.targetPoolName, builder.networkTask);
        this.targetedTaskGenerator = builder.targetedTaskGenerator;
    }

    public Function<Integer, List<Callable<Void>>> getTargetedTaskGenerator() { return targetedTaskGenerator; }

    public static class Builder {
        private String targetPoolName;
        private Callable<Void> networkTask;

        private transient Function<Integer, List<Callable<Void>>> targetedTaskGenerator;

        public Builder poolName(String targetPoolName){
            this.targetPoolName = targetPoolName;
            return this;
        }

        public Builder task(Callable<Void> task){
            this.networkTask = task;
            return this;
        }

        public Builder targetedGenerator(Function<Integer, List<Callable<Void>>> generator){
            this.targetedTaskGenerator = generator;
            return this;
        }

        public CacheJob build(){
            if (targetPoolName == null){
                throw new IllegalStateException("Cannot build CacheJob: poolName is required");

            }
            if (networkTask == null && targetedTaskGenerator == null){
                throw new IllegalStateException("Cannot build CacheJob: Must provide either a networkTask or a targetedTaskGenerator");
            }

            if (networkTask != null && targetedTaskGenerator != null){
                throw new IllegalStateException("Cannot build CacheJob: Can not provide both a networkTask and a targeted generator at the same time");
            }

            return new CacheJob(this);
        }
    }
}
