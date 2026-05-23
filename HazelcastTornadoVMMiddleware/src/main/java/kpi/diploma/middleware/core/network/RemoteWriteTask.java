package kpi.diploma.middleware.core.network;

import kpi.diploma.middleware.core.data.io.RemoteTargetWriter;
import kpi.diploma.middleware.core.logging.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.Callable;

public class RemoteWriteTask<T> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final T metadata;
    private final byte[] content;
    private final RemoteTargetWriter<T> targetWriter;
    private final String workspaceNodePath;


    public RemoteWriteTask(T metadata, byte[] content, RemoteTargetWriter<T> targetWriter, String workspaceNodePath) {
        this.metadata = metadata;
        this.content = content;
        this.targetWriter = targetWriter;
        this.workspaceNodePath = workspaceNodePath;
    }

    @Override
    public Void call(){
        Logger.info("Write", "Initializing write task");
        targetWriter.writeToDisk(metadata, content, workspaceNodePath);
        Logger.info("Write", "Finishing write task");
        return null;
    }
}
