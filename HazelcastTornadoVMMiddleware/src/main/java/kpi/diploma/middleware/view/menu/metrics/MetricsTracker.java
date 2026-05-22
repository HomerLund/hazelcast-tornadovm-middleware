package kpi.diploma.middleware.view.menu.metrics;

public class MetricsTracker {
    public static TaskMetrics measureExecutionTime(String taskName, Runnable task){
        long startTime = System.nanoTime();
        task.run();
        long endTime = System.nanoTime();

        double duration = (endTime - startTime) / 1_000_000.0;
        return new TaskMetrics(taskName, duration);
    }
}
