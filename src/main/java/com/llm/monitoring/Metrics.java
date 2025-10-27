package com.llm.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects metric counters and timers for observability.
 */
public class Metrics {

    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    public void increment(String name) {
        counters.computeIfAbsent(name, k -> new AtomicLong()).incrementAndGet();
    }

    public void incrementBy(String name, long value) {
        counters.computeIfAbsent(name, k -> new AtomicLong()).addAndGet(value);
    }

    public long get(String name) {
        return counters.getOrDefault(name, new AtomicLong()).get();
    }

    public Map<String, AtomicLong> getAll() {
        return counters;
    }
}
