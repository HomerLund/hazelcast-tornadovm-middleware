package kpi.diploma.middleware.server.bootstrap.accelerator;

public interface HardwareAccelerator {
    String getAcceleratorName();
    ComputeGraph createGraph(String graphName);
    void releaseResources();
}
