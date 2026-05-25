package kpi.diploma.middleware.view.menu.builders;

import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.view.menu.metrics.MetricsTracker;
import kpi.diploma.middleware.view.menu.metrics.TaskMetrics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class ResearchConsoleBuilder {
    private final String menuTitle;
    private final Map<String, MenuItem> menuItems = new LinkedHashMap<>();
    private int counter = 1;

    private ResearchConsoleBuilder(String menuTitle){
        this.menuTitle = menuTitle;
    }

    public static ResearchConsoleBuilder create(String title){
        return new ResearchConsoleBuilder(title);
    }

    public ResearchConsoleBuilder addStandardTask(String name, Runnable action){
        menuItems.put(String.valueOf(counter++), new MenuItem(name, action, false));
        return this;
    }

    public ResearchConsoleBuilder addBenchmarkTask(String name, Runnable action){
        menuItems.put(String.valueOf(counter++), new MenuItem(name, action, true));
        return this;
    }

    public void start(){
        Scanner scanner = new Scanner(System.in);
        menuItems.put("0", new MenuItem("Exit", () -> {}, false));

        boolean running = true;
        Logger.success("Menu", "Framework UI initialized successfully");

        while (running){
            drawMenu();
            String choice = scanner.nextLine().trim();

            if(choice.equals("0")){
                running = false;
                continue;
            }

            MenuItem item = menuItems.get(choice);
            if(item != null){
                executeItem(item);
            }
            else{
                Logger.warn("Menu", "Invalid choice '" + choice + "'. Please try again");
            }
        }
    }

    private void drawMenu(){
        Logger.system("UI", "==============================");
        Logger.system("UI", "    " + menuTitle.toUpperCase());
        Logger.system("UI", "==============================");

        for (Map.Entry<String, MenuItem> entry : menuItems.entrySet()){
            String prefix = entry.getValue().isBenchmark()
                    ? "[BENCHMARK]"
                    : "[SYSTEM]";

            Logger.system("UI", "    " + entry.getKey() + ". " + prefix + " " + entry.getValue().name());
        }

        Logger.system("UI", "==============================");
        Logger.system("UI", "Select option (enter number):");
    }

    private void executeItem(MenuItem item){
        Logger.info("Menu", "Starting task: " + item.name());

        if (item.isBenchmark()){
            TaskMetrics metrics = MetricsTracker.measureExecutionTime(item.name(), item.action());
            metrics.printReport();
        }
        else{
            item.action().run();
        }

        Logger.info("Menu", "Finished task: " + item.name());
    }
}
