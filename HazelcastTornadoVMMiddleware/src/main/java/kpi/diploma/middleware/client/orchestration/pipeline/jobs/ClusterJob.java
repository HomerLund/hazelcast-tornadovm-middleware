package kpi.diploma.middleware.client.orchestration.pipeline.jobs;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.Callable;

public abstract class ClusterJob<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    protected final String targetPoolName;
    protected final Callable<T> networkTask;

    protected ClusterJob(String targetPoolName, Callable<T> networkTask){
        this.targetPoolName = targetPoolName;
        this.networkTask = networkTask;
    }

    public String getTargetPoolName(){
        return targetPoolName;
    }

    public Callable<T> getNetworkTask(){
        return networkTask;
    }
}
