package kpi.diploma.middleware.core.context;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeLocalWorkspace {
    private static final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, AtomicInteger> producersCount = new ConcurrentHashMap<>();

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

    @SuppressWarnings("unchecked")
    public static <T> Queue<T> waitForQueue(String key) throws InterruptedException{
        Queue<T> queue = null;
        while (queue == null){
            Object object = cache.get(key);
            if (object instanceof Queue){
                queue = (Queue<T>) object;
            }
            else{
                Thread.sleep(50);
            }
        }
        return queue;
    }

    public static void registerProducers(String queueKey, int count){
        producersCount.putIfAbsent(queueKey, new AtomicInteger(count));
    }

    public static void notifyProducerFinished(String queueKey){
        AtomicInteger count = producersCount.get(queueKey);
        if (count != null){
            count.decrementAndGet();
        }
    }

    public static boolean inQueueFinished(String queueKey){
        AtomicInteger count = producersCount.get(queueKey);
        return count != null && count.get() <= 0;
    }
}
