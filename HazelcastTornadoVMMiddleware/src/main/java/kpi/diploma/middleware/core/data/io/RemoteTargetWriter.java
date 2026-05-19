package kpi.diploma.middleware.core.data.io;

import java.io.Serializable;

public interface RemoteTargetWriter<T> extends Serializable {
    void writeToDisk(T metadata, byte[] content, String workerNodeId);
}
