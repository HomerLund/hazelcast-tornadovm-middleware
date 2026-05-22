package kpi.diploma.middleware.view.menu.metrics;

import kpi.diploma.middleware.core.logging.Logger;

public record TaskMetrics(String taskName, double duration) {
    public void printReport(){
        String reportMessage = String.format("Task '%s' completed in: %.2f ms", taskName, duration);
        Logger.metric("Metrics", reportMessage);
    }
}
