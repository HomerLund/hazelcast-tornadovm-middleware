package kpi.diploma.middleware.core.network;

import kpi.diploma.middleware.core.data.io.RemoteTargetWriter;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.Callable;

public class RemoteWriteTask<T> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final T metadata;
    private final byte[] content;
    private final RemoteTargetWriter<T> targetWriter;
    private final String targetNodeId;


    public RemoteWriteTask(T metadata, byte[] content, RemoteTargetWriter<T> targetWriter, String targetNodeId) {
        this.metadata = metadata;
        this.content = content;
        this.targetWriter = targetWriter;
        this.targetNodeId = targetNodeId;
    }

    @Override
    public Void call(){
        targetWriter.writeToDisk(metadata, content, targetNodeId);
        return null;
    }
}
