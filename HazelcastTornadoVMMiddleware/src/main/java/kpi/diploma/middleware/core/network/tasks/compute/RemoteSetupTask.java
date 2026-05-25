package kpi.diploma.middleware.core.network.tasks.compute;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.SerializableFunction;
import kpi.diploma.middleware.core.logging.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class RemoteSetupTask<O> implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final SerializableFunction<Void, O> userLambda;
    private final String cashKey;

    public RemoteSetupTask(SerializableFunction<Void, O> userLambda, String cashKey) {
        this.userLambda = userLambda;
        this.cashKey = cashKey;
    }

    @Override
    public Void call() throws Exception {
        try {
            O result = userLambda.apply(null);

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
