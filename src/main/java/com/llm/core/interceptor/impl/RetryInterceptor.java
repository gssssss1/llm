package com.llm.core.interceptor.impl;

import com.llm.core.interceptor.Interceptor;
import com.llm.exception.ProviderException;
import com.llm.exception.RateLimitException;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Random;

/**
 * Interceptor that retries failed requests with exponential backoff and jitter.
 */
public class RetryInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
    private static final Random RANDOM = new Random();

    private final int maxRetries;
    private final Duration initialBackoff;
    private final Duration maxBackoff;

    public RetryInterceptor() {
        this(3, Duration.ofMillis(500), Duration.ofSeconds(5));
    }

    public RetryInterceptor(int maxRetries, Duration initialBackoff, Duration maxBackoff) {
        this.maxRetries = maxRetries;
        this.initialBackoff = initialBackoff;
        this.maxBackoff = maxBackoff;
    }

    @Override
    public Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        int attempt = 0;
        Duration backoff = initialBackoff;

        while (true) {
            try {
                return chain.proceed(request);
            } catch (RateLimitException e) {
                attempt++;
                if (attempt > maxRetries) {
                    throw e;
                }
                long retryDelay = e.getRetryAfterSeconds() != null
                        ? e.getRetryAfterSeconds() * 1000L
                        : backoff.toMillis();
                sleepWithJitter(retryDelay);
                backoff = backoff.multipliedBy(2);
                if (backoff.compareTo(maxBackoff) > 0) {
                    backoff = maxBackoff;
                }
            } catch (ProviderException e) {
                attempt++;
                if (attempt > maxRetries) {
                    throw e;
                }
                sleepWithJitter(backoff.toMillis());
                backoff = backoff.multipliedBy(2);
                if (backoff.compareTo(maxBackoff) > 0) {
                    backoff = maxBackoff;
                }
            }
        }
    }

    private void sleepWithJitter(long baseMillis) {
        long jitter = RANDOM.nextInt((int) Math.max(1, baseMillis / 2));
        long delay = baseMillis + jitter;
        logger.debug("Retrying after {} ms", delay);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }
}
