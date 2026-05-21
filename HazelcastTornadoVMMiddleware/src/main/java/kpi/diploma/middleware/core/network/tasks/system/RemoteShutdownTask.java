package kpi.diploma.middleware.core.network.tasks.system;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;

import java.io.Serializable;

public class RemoteShutdownTask implements Runnable, Serializable, HazelcastInstanceAware {
    private transient HazelcastInstance localInstance;

    @Override
    public void setHazelcastInstance(HazelcastInstance instance){
        this.localInstance = instance;
    }

    @Override
    public void run(){
        System.out.println("[System] Received remote shutdown command...");

        new Thread(() -> {
            try{
                Thread.sleep(2000);
                System.out.println("[System] Executing graceful shutdown...");
                localInstance.getLifecycleService().terminate();
                System.exit(0);
            }
            catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
