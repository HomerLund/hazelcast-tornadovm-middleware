package kpi.diploma.middleware.client.api.context;

import java.util.function.Supplier;

public interface GpuContext {
    <T> T executeOnGpu(String cacheKey, Object memoryContext, Supplier<T> gpuLogic);
}
