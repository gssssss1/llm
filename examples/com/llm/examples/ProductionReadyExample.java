package com.llm.examples;

import com.llm.config.ExecutionConfig;
import com.llm.config.ModelConfig;
import com.llm.config.SecurityConfig;
import com.llm.core.interceptor.InterceptorChain;
import com.llm.core.interceptor.impl.*;
import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionConfig;
import com.llm.core.session.SessionFactory;
import com.llm.monitoring.Metrics;
import com.llm.monitoring.MetricsReporter;
import com.llm.provider.ProviderType;
import com.llm.security.BasicContentFilter;

import java.time.Duration;

/**
 * Production-ready example with comprehensive configuration.
 */
public class ProductionReadyExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();
        Metrics metrics = factory.getGlobalMetrics();

        ModelConfig modelConfig = ModelConfig.builder()
                .provider(ProviderType.OPENAI.getId())
                .model("gpt-3.5-turbo")
                .parameter("temperature", 0.7)
                .parameter("max_tokens", 1000)
                .build();

        ExecutionConfig executionConfig = ExecutionConfig.builder()
                .timeout(Duration.ofSeconds(30))
                .maxRetries(3)
                .retryDelay(Duration.ofSeconds(1))
                .rateLimit(100)
                .circuitBreakerEnabled(true)
                .circuitBreakerThreshold(5)
                .build();

        SecurityConfig securityConfig = SecurityConfig.builder()
                .contentFilterEnabled(true)
                .dataMaskingEnabled(true)
                .auditLogEnabled(true)
                .build();

        SessionConfig sessionConfig = SessionConfig.builder()
                .modelConfig(modelConfig)
                .executionConfig(executionConfig)
                .securityConfig(securityConfig)
                .build();

        InterceptorChain chain = InterceptorChain.builder()
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new MetricsInterceptor(metrics))
                .addInterceptor(new CachingInterceptor(Duration.ofMinutes(15), 1000))
                .addInterceptor(new RetryInterceptor(3, Duration.ofMillis(500), Duration.ofSeconds(5)))
                .addInterceptor(new RateLimitInterceptor(100, Duration.ofMinutes(1)))
                .addInterceptor(new CircuitBreakerInterceptor(5, Duration.ofSeconds(30)))
                .addInterceptor(new ContentFilterInterceptor(new BasicContentFilter(), false))
                .addInterceptor(new ErrorHandlerInterceptor())
                .build();

        try (LLMSession session = factory.builder()
                .sessionConfig(sessionConfig)
                .interceptorChain(chain)
                .build()) {

            session.send(UserMessage.of("What are the key principles of building production-ready systems?"));

            MetricsReporter reporter = new MetricsReporter(metrics);
            reporter.report();

            System.out.println("\nDetailed Metrics:");
            System.out.println("Total Requests: " + metrics.get("requests.total"));
            System.out.println("Success Rate: " + 
                (double) metrics.get("requests.success") / metrics.get("requests.total") * 100 + "%");
            System.out.println("Average Latency: " + 
                metrics.getAverageLatencyMillis("openai") + "ms");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
