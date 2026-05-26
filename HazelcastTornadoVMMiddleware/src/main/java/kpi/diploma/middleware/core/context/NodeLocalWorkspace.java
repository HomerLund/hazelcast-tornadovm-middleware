package kpi.diploma.middleware.core.context;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NodeLocalWorkspace {
    private static final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    private static final AtomicBoolean endOfStream = new AtomicBoolean(false);

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

    @SuppressWarnings("unchecked")
    public static <T>Queue<T> getOrCreateQueue(String key){
        return (Queue<T>) cache.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<T>());
    }

    @SuppressWarnings("unchecked")
    public static <T> BlockingQueue<T> getOrCreateBlockingQueue(String key, int capacity){
        return (BlockingQueue<T>) cache.computeIfAbsent(key, k -> new LinkedBlockingQueue<>(capacity));
    }

    public static void setEndOfStream(boolean isEnd){
        endOfStream.set(isEnd);
    }

    public static boolean isEndOfStream(){
        return endOfStream.get();
    }
}
