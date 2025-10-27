package com.llm.core.interceptor.impl;

import com.llm.core.interceptor.Interceptor;
import com.llm.exception.LLMException;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker to prevent cascading failures.
 */
public class CircuitBreakerInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerInterceptor.class);

    private enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private final int failureThreshold;
    private final Duration timeout;
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private volatile Instant openedAt;

    public CircuitBreakerInterceptor(int failureThreshold, Duration timeout) {
        this.failureThreshold = failureThreshold;
        this.timeout = timeout;
    }

    @Override
    public Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        State currentState = state.get();

        if (currentState == State.OPEN) {
            if (Instant.now().isAfter(openedAt.plus(timeout))) {
                logger.info("Circuit breaker transitioning to HALF_OPEN");
                state.set(State.HALF_OPEN);
                successCount.set(0);
            } else {
                throw new LLMException("Circuit breaker is OPEN");
            }
        }

        try {
            Response response = chain.proceed(request);
            onSuccess();
            return response;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    private void onSuccess() {
        consecutiveFailures.set(0);

        if (state.get() == State.HALF_OPEN) {
            if (successCount.incrementAndGet() >= 3) {
                logger.info("Circuit breaker transitioning to CLOSED");
                state.set(State.CLOSED);
            }
        }
    }

    private void onFailure() {
        int failures = consecutiveFailures.incrementAndGet();

        if (failures >= failureThreshold && state.get() != State.OPEN) {
            logger.warn("Circuit breaker opening after {} failures", failures);
            state.set(State.OPEN);
            openedAt = Instant.now();
        }
    }

    public State getState() {
        return state.get();
    }

    public void reset() {
        state.set(State.CLOSED);
        consecutiveFailures.set(0);
        successCount.set(0);
    }
}
