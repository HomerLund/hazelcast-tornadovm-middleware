package kpi.diploma.middleware.server.adapters.accelerator.execution;

import kpi.diploma.middleware.client.api.context.GpuContext;
import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.server.bootstrap.accelerator.ComputeGraph;
import kpi.diploma.middleware.server.bootstrap.accelerator.ComputePlan;
import kpi.diploma.middleware.server.bootstrap.accelerator.HardwareAccelerator;
import kpi.diploma.middleware.server.bootstrap.accelerator.instrumentation.GpuKernelInterceptor;

import java.util.function.Supplier;

public class ServerGpuContext implements GpuContext {
    private final HardwareAccelerator accelerator;

    public ServerGpuContext(HardwareAccelerator accelerator){
        this.accelerator = accelerator;
    }

    @Override
    public <T> T executeOnGpu(String cacheKey, Object memoryContext, Supplier<T> gpuLogic) {
        ComputePlan cachedPlan = NodeLocalWorkspace.get(cacheKey);
        if (cachedPlan == null){
            Logger.info("ServerGpuContext", "Graph not found. Starting JIT tracing for the key: " + cacheKey);
            ComputeGraph graph = accelerator.createGraph("dynamic_plan_" + System.nanoTime());

            GpuKernelInterceptor.startTracing(graph);

            gpuLogic.get();

            GpuKernelInterceptor.stopTracing();

            if (memoryContext != null){
                GpuMemoryExtractor.ExtractGpuBuffers buffers = GpuMemoryExtractor.extractAnnotationBuffers(memoryContext);

                if (buffers.onceBuffers().length > 0){
                    graph.allocateOnDevice(buffers.onceBuffers());
                }

                if (buffers.everExecutionBuffers().length > 0){
                    graph.copyToDevice(buffers.everExecutionBuffers());
                }
            }

            cachedPlan = graph.compile();

            NodeLocalWorkspace.put(cacheKey, cachedPlan);
            Logger.info("ServerGpuContext", "Plan has been successfully compiled and saved in NodeLocalWorkspace");
        }

        cachedPlan.execute();

        return gpuLogic.get();
    }

}
