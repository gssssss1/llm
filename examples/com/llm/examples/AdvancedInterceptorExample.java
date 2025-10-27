package com.llm.examples;

import com.llm.core.interceptor.InterceptorChain;
import com.llm.core.interceptor.impl.*;
import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.monitoring.Metrics;
import com.llm.provider.ProviderType;
import com.llm.security.BasicContentFilter;

import java.time.Duration;

/**
 * Advanced example demonstrating multiple interceptors working together.
 */
public class AdvancedInterceptorExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();
        Metrics metrics = new Metrics();

        InterceptorChain chain = InterceptorChain.builder()
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new MetricsInterceptor(metrics))
                .addInterceptor(new CachingInterceptor())
                .addInterceptor(new RetryInterceptor(3, Duration.ofMillis(500), Duration.ofSeconds(5)))
                .addInterceptor(new RateLimitInterceptor(10, Duration.ofMinutes(1)))
                .addInterceptor(new CircuitBreakerInterceptor(5, Duration.ofSeconds(30)))
                .addInterceptor(new ContentFilterInterceptor(new BasicContentFilter(), false))
                .build();

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .systemPrompt("You are a helpful assistant.")
                .interceptorChain(chain)
                .build()) {

            // First call - will be executed and cached
            session.send(UserMessage.of("What is machine learning?"));

            // Second identical call - will be served from cache
            session.send(UserMessage.of("What is machine learning?"));

            // Display metrics
            System.out.println("\nMetrics:");
            System.out.println("Total requests: " + metrics.get("requests.total"));
            System.out.println("Successful requests: " + metrics.get("requests.success"));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
