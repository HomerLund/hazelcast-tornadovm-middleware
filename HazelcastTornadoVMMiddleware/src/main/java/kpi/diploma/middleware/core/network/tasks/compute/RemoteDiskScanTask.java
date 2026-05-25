package kpi.diploma.middleware.core.network.tasks.compute;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.core.network.MiddlewareConstants;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RemoteDiskScanTask<O> implements Callable<Void>, Serializable, HazelcastInstanceAware {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String baseDirectory;
    private final SerializableFunction<String, O> userLambda;
    private final String cashKey;

    private transient HazelcastInstance hazelcastInstance;

    public RemoteDiskScanTask(String baseDirectory, SerializableFunction<String, O> userLambda, String cashKey) {
        this.baseDirectory = baseDirectory;
        this.userLambda = userLambda;
        this.cashKey = cashKey;
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance){
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public Void call() throws Exception {
        try {
            Member localMember = hazelcastInstance.getCluster().getLocalMember();

            String host = localMember.getAddress().getHost();
            int port = localMember.getAddress().getPort();

            String nodeName = "node-" + host.replace(".", "-") + "_" + port;

            String finalPath =  Paths.get(baseDirectory, MiddlewareConstants.SYSTEM_SANDBOX_FOLDER_NAME, nodeName).toString();

            O result = userLambda.apply(finalPath);

            if (cashKey != null && result != null) {
                if (result instanceof Collection<?> collection) {
                    ConcurrentLinkedQueue<?> safeQueue = new ConcurrentLinkedQueue<>(collection);
                    NodeLocalWorkspace.put(cashKey, safeQueue);

                    Logger.success("Node Cache", "Successfully cached Thread-Safe Queue (size: "
                            + collection.size() + ") under key: '" + cashKey + "'");

                } else {
                    NodeLocalWorkspace.put(cashKey, result);
                    Logger.success("Node Cache", "Successfully cached Object under key: '" + cashKey + "'");
                }
            }
        }
        catch (Exception e){
            Logger.error("Node Cache", "Error executing RemoteSetupTask: " + e.getMessage());
            throw new RuntimeException("Failed to setup node cache");
        }

        return null;
    }
}
