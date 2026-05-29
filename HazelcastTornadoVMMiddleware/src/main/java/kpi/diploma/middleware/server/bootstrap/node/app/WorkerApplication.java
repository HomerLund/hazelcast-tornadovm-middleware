package kpi.diploma.middleware.server.bootstrap.node.app;

import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.core.network.MiddlewareConstants;
import kpi.diploma.middleware.server.adapters.accelerator.execution.ServerGpuContext;
import kpi.diploma.middleware.server.adapters.accelerator.tornadovm.TornadoVMAccelerator;
import kpi.diploma.middleware.server.bootstrap.accelerator.HardwareAccelerator;
import kpi.diploma.middleware.server.bootstrap.node.ClusterWorkerNode;
import kpi.diploma.middleware.server.bootstrap.node.config.PropertyFileReader;
import kpi.diploma.middleware.server.bootstrap.node.factory.WorkerNodeFactory;
import net.bytebuddy.agent.ByteBuddyAgent;

import java.util.Properties;

public class WorkerApplication {
    public static void run(String propertiesFilePath){
        System.out.println("Starting worker...");

        try{
            ByteBuddyAgent.install();
            System.out.println("ByteBuddy Agent successfully installed");
        }
        catch (Exception e){
            System.err.println("Failed to install ByteBuddy Agent: " + e.getMessage());
        }

        try{
            HardwareAccelerator accelerator = new TornadoVMAccelerator();
            ServerGpuContext nodeGpuContext = new ServerGpuContext(accelerator);
            NodeLocalWorkspace.put(MiddlewareConstants.SYSTEM_ACCELERATOR_CACHE_KEY, nodeGpuContext);

            System.out.println("GPU Context successfully registered in NodeLocalWorkspace");
        }
        catch (Exception e){
            System.err.println("Failed to initialize GPU Accelerator: " + e.getMessage());
        }

        try {
            Properties properties = PropertyFileReader.read(propertiesFilePath);

            ClusterWorkerNode node = WorkerNodeFactory.createNode(properties);

            Runtime.getRuntime().addShutdownHook(new Thread(() ->{
                System.out.println("OS termination signal detected. Shutting down gracefully...");
                node.shutdown();
            }));

            node.start();
        }
        catch (Exception e){
            System.err.println("Failed to start worker node: " + e.getMessage());
            System.exit(1);
        }
    }
}
