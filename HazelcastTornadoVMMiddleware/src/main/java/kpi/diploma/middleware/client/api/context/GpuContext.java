package kpi.diploma.middleware.client.api.context;

import kpi.diploma.middleware.server.adapters.accelerator.execution.GpuMemoryExtractor;

import java.util.function.Supplier;

public interface GpuContext {
    <T> T executeOnGpu(String cacheKey, Object memoryContext, Supplier<T> gpuLogic);
    void syncToHost(String cacheKey);
}
