package com.llm.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reports metrics in various formats.
 */
public class MetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(MetricsReporter.class);

    private final Metrics metrics;

    public MetricsReporter(Metrics metrics) {
        this.metrics = metrics;
    }

    public void report() {
        logger.info("====== Metrics Report ======");
        logger.info("Total Requests: {}", metrics.get("requests.total"));
        logger.info("Successful Requests: {}", metrics.get("requests.success"));
        logger.info("Failed Requests: {}", metrics.get("requests.failure"));
        logger.info("===========================");
    }

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Metrics Report\n");
        sb.append("=============\n");

        Map<String, AtomicLong> allMetrics = metrics.getAll();
        allMetrics.forEach((key, value) -> {
            sb.append(String.format("%-40s: %d\n", key, value.get()));
        });

        return sb.toString();
    }

    public String generateJsonReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        Map<String, AtomicLong> allMetrics = metrics.getAll();
        boolean first = true;
        for (Map.Entry<String, AtomicLong> entry : allMetrics.entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            sb.append(String.format("  \"%s\": %d", entry.getKey(), entry.getValue().get()));
            first = false;
        }

        sb.append("\n}");
        return sb.toString();
    }
}
