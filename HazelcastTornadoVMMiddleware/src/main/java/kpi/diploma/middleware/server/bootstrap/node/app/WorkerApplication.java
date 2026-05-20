package kpi.diploma.middleware.server.bootstrap.node.app;

import kpi.diploma.middleware.server.bootstrap.node.ClusterWorkerNode;
import kpi.diploma.middleware.server.bootstrap.node.config.PropertyFileReader;
import kpi.diploma.middleware.server.bootstrap.node.factory.WorkerNodeFactory;

import java.util.Properties;

public class WorkerApplication {
    public static void run(String propertiesFilePath){
        System.out.println("Starting worker...");

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
