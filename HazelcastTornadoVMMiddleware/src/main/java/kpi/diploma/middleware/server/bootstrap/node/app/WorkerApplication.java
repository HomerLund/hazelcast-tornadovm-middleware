package kpi.diploma.middleware.server.bootstrap.node.app;

import kpi.diploma.middleware.client.api.gpu.GpuKernel;
import kpi.diploma.middleware.client.api.gpu.GpuParallel;
import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.core.network.MiddlewareConstants;
import kpi.diploma.middleware.server.adapters.accelerator.execution.ServerGpuContext;
import kpi.diploma.middleware.server.adapters.accelerator.tornadovm.TornadoVMAccelerator;
import kpi.diploma.middleware.server.adapters.accelerator.tornadovm.bytebuddy.TornadoVMAnnotationRemapper;
import kpi.diploma.middleware.server.bootstrap.accelerator.HardwareAccelerator;
import kpi.diploma.middleware.server.bootstrap.accelerator.instrumentation.ByteBuddyAgentInstaller;
import kpi.diploma.middleware.server.bootstrap.accelerator.instrumentation.GpuKernelInterceptor;
import kpi.diploma.middleware.server.bootstrap.node.ClusterWorkerNode;
import kpi.diploma.middleware.server.bootstrap.node.config.PropertyFileReader;
import kpi.diploma.middleware.server.bootstrap.node.factory.WorkerNodeFactory;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.jar.asm.*;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

import javax.swing.text.Element;
import java.util.Properties;

public class WorkerApplication {
    public static void run(String propertiesFilePath){
        System.out.println("Starting worker...");

        AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper annotationRemapper = new TornadoVMAnnotationRemapper();
        ByteBuddyAgentInstaller.install(annotationRemapper);

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
