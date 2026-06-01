package kpi.diploma.middleware.server.adapters.accelerator.tornadovm;

import kpi.diploma.middleware.server.bootstrap.accelerator.ComputePlan;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.TornadoExecutionResult;

public class TornadoVMPlan implements ComputePlan {
    private final TornadoExecutionPlan plan;
    private TornadoExecutionResult result;

    public TornadoVMPlan(TornadoExecutionPlan plan) {
        this.plan = plan;
    }

    @Override
    public void execute() {
        result = plan.execute();
    }

    @Override
    public void syncToHost(Object... buffers) {
        if (result != null && buffers.length > 0){
            result.transferToHost(buffers);
        }
    }
}
