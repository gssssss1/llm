package com.llm.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks cost of API usage.
 */
public class CostTracker {

    private final Map<String, AtomicReference<Double>> costPerProvider = new ConcurrentHashMap<>();

    public void addCost(String provider, double cost) {
        costPerProvider.computeIfAbsent(provider, p -> new AtomicReference<>(0.0))
                .updateAndGet(current -> current + cost);
    }

    public double getCost(String provider) {
        return costPerProvider.getOrDefault(provider, new AtomicReference<>(0.0)).get();
    }

    public Map<String, AtomicReference<Double>> getAll() {
        return costPerProvider;
    }
}
