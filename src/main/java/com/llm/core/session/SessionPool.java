package com.llm.core.session;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Simple session pool implementation with configurable limits.
 */
public class SessionPool {

    public enum EvictionPolicy {
        FIFO,
        LRU,
        LFU,
        TTL
    }

    private final SessionFactory factory;
    private final SessionConfig config;
    private final int minIdle;
    private final int maxTotal;
    private final Duration maxWaitTime;
    private final EvictionPolicy evictionPolicy;

    private final Deque<LLMSession> idleSessions = new ArrayDeque<>();
    private int total;

    public SessionPool(SessionFactory factory, SessionConfig config, int minIdle, int maxTotal, Duration maxWaitTime, EvictionPolicy evictionPolicy) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.config = Objects.requireNonNull(config, "config");
        this.minIdle = minIdle;
        this.maxTotal = maxTotal;
        this.maxWaitTime = maxWaitTime != null ? maxWaitTime : Duration.ofSeconds(10);
        this.evictionPolicy = evictionPolicy != null ? evictionPolicy : EvictionPolicy.FIFO;

        for (int i = 0; i < minIdle; i++) {
            idleSessions.add(createSession());
            total++;
        }
    }

    public synchronized LLMSession borrow() {
        if (!idleSessions.isEmpty()) {
            return idleSessions.poll();
        }

        if (total < maxTotal) {
            total++;
            return createSession();
        }

        long deadline = System.currentTimeMillis() + maxWaitTime.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (!idleSessions.isEmpty()) {
                return idleSessions.poll();
            }
            try {
                wait(maxWaitTime.toMillis() / 10);
            } catch (InterruptedException ignored) {
            }
        }

        throw new IllegalStateException("No session available in pool");
    }

    public synchronized void release(LLMSession session) {
        if (session == null) {
            return;
        }
        if (idleSessions.size() >= maxTotal) {
            session.close();
            total--;
            return;
        }
        idleSessions.offer(session);
        notifyAll();
    }

    private LLMSession createSession() {
        return factory.fromConfig(config);
    }
}
