package util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;

/**
 * Thread-safe простая реализация Metrics.
 */
public class SimpleMetrics implements Metrics {
    private final Map<String, LongAdder> counts = new ConcurrentHashMap<>();
    private final Map<String, Long> timers = new ConcurrentHashMap<>();
    private final Map<String, Long> starts = new ConcurrentHashMap<>();

    @Override
    public void inc(String name) {
        counts.computeIfAbsent(name, k -> new LongAdder()).increment();
    }

    @Override
    public long getCount(String name) {
        LongAdder adder = counts.get(name);
        return adder == null ? 0L : adder.longValue();
    }

    @Override
    public void startTimer(String name) {
        starts.put(name, System.nanoTime());
    }

    @Override
    public void stopTimer(String name) {
        Long s = starts.remove(name);
        if (s != null) {
            timers.merge(name, System.nanoTime() - s, Long::sum);
        }
    }

    @Override
    public long getTime(String name) {
        return timers.getOrDefault(name, 0L);
    }
}
