package com.llm.examples;

import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.monitoring.Metrics;
import com.llm.monitoring.MetricsReporter;
import com.llm.provider.ProviderType;

/**
 * Demonstrates metrics collection and reporting.
 */
public class MonitoringExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();
        Metrics metrics = factory.getGlobalMetrics();
        MetricsReporter reporter = new MetricsReporter(metrics);

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .build()) {

            session.send(UserMessage.of("Summarize the benefits of monitoring."));
            session.send(UserMessage.of("What metrics should be tracked for LLM applications?"));

            System.out.println(reporter.generateReport());
            System.out.println(reporter.generateJsonReport());
        }
    }
}
