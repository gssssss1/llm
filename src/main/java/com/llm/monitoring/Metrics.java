package com.llm.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects metric counters and timers for observability.
 */
public class Metrics {

    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> latencies = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> latencySamples = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> promptTokens = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> completionTokens = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalTokens = new ConcurrentHashMap<>();

    public void increment(String name) {
        counters.computeIfAbsent(name, k -> new AtomicLong()).incrementAndGet();
    }

    public void incrementBy(String name, long value) {
        counters.computeIfAbsent(name, k -> new AtomicLong()).addAndGet(value);
    }

    public long get(String name) {
        return counters.getOrDefault(name, new AtomicLong()).get();
    }

    public void recordLatency(String provider, long nanos) {
        String latencyKey = "latency." + provider;
        latencies.computeIfAbsent(latencyKey, k -> new AtomicLong()).addAndGet(nanos);
        latencySamples.computeIfAbsent(latencyKey, k -> new AtomicLong()).incrementAndGet();
    }

    public double getAverageLatencyMillis(String provider) {
        String latencyKey = "latency." + provider;
        long totalNanos = latencies.getOrDefault(latencyKey, new AtomicLong()).get();
        long samples = latencySamples.getOrDefault(latencyKey, new AtomicLong()).get();
        if (samples == 0) {
            return 0.0;
        }
        return (totalNanos / 1_000_000.0) / samples;
    }

    public void recordTokens(String provider, com.llm.provider.common.Response.Usage usage) {
        promptTokens.computeIfAbsent(provider, k -> new AtomicLong()).addAndGet(usage.getPromptTokens());
        completionTokens.computeIfAbsent(provider, k -> new AtomicLong()).addAndGet(usage.getCompletionTokens());
        totalTokens.computeIfAbsent(provider, k -> new AtomicLong()).addAndGet(usage.getTotalTokens());
    }

    public long getPromptTokens(String provider) {
        return promptTokens.getOrDefault(provider, new AtomicLong()).get();
    }

    public long getCompletionTokens(String provider) {
        return completionTokens.getOrDefault(provider, new AtomicLong()).get();
    }

    public long getTotalTokens(String provider) {
        return totalTokens.getOrDefault(provider, new AtomicLong()).get();
    }

    public void logRequest(String provider, long durationNanos) {
        increment("requests.logged");
        recordLatency(provider, durationNanos);
    }

    public Map<String, AtomicLong> getAll() {
        return counters;
    }
}
