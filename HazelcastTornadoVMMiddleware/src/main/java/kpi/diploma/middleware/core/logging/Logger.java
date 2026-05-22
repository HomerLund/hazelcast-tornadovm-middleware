package kpi.diploma.middleware.core.logging;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static void log(String level, String component, String message, String color){
        String time = LocalTime.now().format(TIME_FORMATTER);

        String componentTag = (component != null && !component.isBlank())
                ? "[" + component + "] "
                : "";

        System.out.println(color + time + " " + level + " " + componentTag + message + RESET);
    }

    public static void info(String component, String message){
        log("[INFO]    ", component, message, RESET);
    }

    public static void success(String component, String message){
        log("[SUCCESS]    ", component, message, GREEN);
    }

    public static void warn(String component, String message){
        log("[WARN]    ", component, message, YELLOW);
    }

    public static void error(String component, String message){
        log("[ERROR]    ", component, message, RED);
    }

    public static void system(String component, String message){
        log("[SYSTEM]    ", component, message, BLUE);
    }

    public static void metric(String component, String message){
        log("[METRIC]    ", component, message, CYAN);
    }
}
