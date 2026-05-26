package kpi.diploma.middleware.core.network.tasks.compute.cache;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.logging.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.Callable;

public class RemoteRamCacheSetupTask<O> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final O partitionData;
    private final String cacheKey;

    public RemoteRamCacheSetupTask(O partitionData, String cacheKey) {
        this.partitionData = partitionData;
        this.cacheKey = cacheKey;
    }

    @Override
    public Void call() throws Exception {
        try {
            NodeLocalWorkspace.put(cacheKey, partitionData);
            Logger.success("Node Cache", "RAM Cache initialized successfully under key: '" + cacheKey + "'");
        }
        catch (Exception e){
            Logger.error("Node Cache", "Failed to setup RAM cache: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }
}
