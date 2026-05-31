package kpi.diploma.middleware.server.bootstrap.accelerator.instrumentation;

import kpi.diploma.middleware.client.api.gpu.GpuKernel;
import kpi.diploma.middleware.server.bootstrap.accelerator.ComputeGraph;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

public class GpuKernelInterceptor {
    private static final ThreadLocal<ComputeGraph> currentGraph = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isTracing = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> isBypassing = ThreadLocal.withInitial(() -> false);

    public static void startTracing(ComputeGraph graph){
        currentGraph.set(graph);
        isTracing.set(true);
    }

    public static void stopTracing(){
        currentGraph.remove();
        isTracing.set(false);
    }

    public static void startBypassing(){
        isBypassing.set(true);
    }

    public static void stopBypassing(){
        isBypassing.set(false);
    }

    @RuntimeType
    public static Object intercept(@AllArguments Object[] args, @Origin Method method) throws Exception{
        if (isTracing.get()){
            ComputeGraph graph = currentGraph.get();
            String kernelName = method.getAnnotation(GpuKernel.class).name();
            graph.addDynamicKernel(kernelName, method, args);
            return null;
        } else if (isBypassing.get()) {
            return null;
        }
        else{
            return method.invoke(null, args);
        }
    }

}
