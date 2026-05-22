package kpi.diploma.middleware.client.orchestration.distribution;

import kpi.diploma.middleware.core.data.distribution.DataPartitioner;
import kpi.diploma.middleware.core.data.io.RemoteSourceLoader;
import kpi.diploma.middleware.core.data.io.RemoteTargetWriter;

import java.util.List;

public class DistributionJob<T> {
    private final List<T> allItems;
    private final DataPartitioner<T> partitioner;
    private final RemoteSourceLoader<T> sourceLoader;
    private final RemoteTargetWriter<T> targetWriter;
    private final String workspacePath;
    private final double[] customProportions;

    private DistributionJob(Builder<T> builder){
        this.allItems = builder.allItems;
        this.partitioner = builder.partitioner;
        this.sourceLoader = builder.sourceLoader;
        this.targetWriter = builder.targetWriter;
        this.workspacePath = builder.workspacePath;
        this.customProportions = builder.customProportions;
    }

    public List<T> getAllItems() {return allItems;}
    public DataPartitioner<T> getPartitioner() {return partitioner;}
    public RemoteSourceLoader<T> getSourceLoader() {return sourceLoader;}
    public RemoteTargetWriter<T> getTargetWriter() {return targetWriter;}
    public String getWorkspacePath() {return workspacePath;}
    public double[] getCustomProportions() {return customProportions;}

    private static class Builder<T>{
        private List<T> allItems;
        private DataPartitioner<T> partitioner;
        private RemoteSourceLoader<T> sourceLoader;
        private RemoteTargetWriter<T> targetWriter;
        private String workspacePath;
        private double[] customProportions;

        public Builder<T> items(List<T> items){
            this.allItems = items;
            return this;
        }

        public Builder<T> partitioner(DataPartitioner<T> partitioner){
            this.partitioner = partitioner;
            return this;
        }

        public Builder<T> loader(RemoteSourceLoader<T> loader){
            this.sourceLoader = loader;
            return this;
        }

        public Builder<T> writer(RemoteTargetWriter<T> writer){
            this.targetWriter = writer;
            return this;
        }

        public Builder<T> workspace(String workspace){
            this.workspacePath = workspace;
            return this;
        }

        public Builder<T> proportions(double[] proportions){
            this.customProportions = proportions;
            return this;
        }

        public DistributionJob<T> build(){
            return new DistributionJob<>(this);
        }
    }
}
