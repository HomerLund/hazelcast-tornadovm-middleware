package kpi.diploma.middleware.core.context;

import java.util.concurrent.ConcurrentHashMap;

public class NodeLocalWorkspace {
    private static final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    private NodeLocalWorkspace(){}

    public static void put(String key, Object value){
        cache.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key){
        return (T) cache.get(key);
    }

    public static void remove(String key){
        cache.remove(key);
    }

    public static void clear(){
        cache.clear();
    }
}
