package kpi.diploma.middleware.server.bootstrap.accelerator;

import java.lang.reflect.Method;

public interface ComputeGraph {
    ComputeGraph allocateOnDevice(Object... memoryBuffers);
    ComputeGraph copyToDevice(Object... memoryBuffers);
    ComputeGraph addDynamicKernel(String name, Method method, Object[] args);
    ComputeGraph copyToHost(Object... memoryBuffers);
    ComputePlan compile();
}
