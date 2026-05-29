package kpi.diploma.middleware.server.adapters.accelerator.tornadovm;

import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.server.bootstrap.accelerator.ComputeGraph;
import kpi.diploma.middleware.server.bootstrap.accelerator.HardwareAccelerator;

public class TornadoVMAccelerator implements HardwareAccelerator {
    public TornadoVMAccelerator(){
        Logger.success("TornadoVMAccelerator", "Backend initialized successfully");
    }

    @Override
    public String getAcceleratorName() {
        return "TornadoVM";
    }

    @Override
    public ComputeGraph createGraph(String graphName) {
        return new TornadoVMGraph(graphName);
    }

    @Override
    public void releaseResources() {

    }
}
