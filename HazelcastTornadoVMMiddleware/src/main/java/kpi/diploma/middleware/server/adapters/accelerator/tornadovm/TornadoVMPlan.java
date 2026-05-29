package kpi.diploma.middleware.server.adapters.accelerator.tornadovm;

import kpi.diploma.middleware.server.bootstrap.accelerator.ComputePlan;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;

public class TornadoVMPlan implements ComputePlan {
    private final TornadoExecutionPlan plan;

    public TornadoVMPlan(TornadoExecutionPlan plan) {
        this.plan = plan;
    }

    @Override
    public void execute() {
        plan.execute();
    }
}
