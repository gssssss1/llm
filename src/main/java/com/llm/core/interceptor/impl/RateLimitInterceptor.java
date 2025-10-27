package com.llm.core.interceptor.impl;

import com.llm.core.interceptor.Interceptor;
import com.llm.exception.RateLimitException;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Rate limiting interceptor using token bucket and sliding window algorithms.
 */
public class RateLimitInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Queue<Instant> requestTimestamps = new LinkedList<>();

    public RateLimitInterceptor(int maxRequestsPerWindow, Duration windowDuration) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
    }

    @Override
    public synchronized Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(windowDuration);

        while (!requestTimestamps.isEmpty() && requestTimestamps.peek().isBefore(windowStart)) {
            requestTimestamps.poll();
        }

        if (requestTimestamps.size() >= maxRequestsPerWindow) {
            Instant oldestRequest = requestTimestamps.peek();
            long waitTimeMs = Duration.between(now, oldestRequest.plus(windowDuration)).toMillis();
            logger.warn("Rate limit exceeded. Need to wait {} ms", waitTimeMs);
            throw new RateLimitException("Rate limit exceeded", waitTimeMs / 1000);
        }

        requestTimestamps.offer(now);
        return chain.proceed(request);
    }

    public synchronized int getCurrentRequestCount() {
        Instant now = Instant.now();
        Instant windowStart = now.minus(windowDuration);

        while (!requestTimestamps.isEmpty() && requestTimestamps.peek().isBefore(windowStart)) {
            requestTimestamps.poll();
        }

        return requestTimestamps.size();
    }
}
