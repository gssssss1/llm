package com.llm.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Advanced session pool implementation with health checks and metrics.
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
    private final Duration sessionTtl;
    private final Map<LLMSession, SessionMetadata> metadata = new ConcurrentHashMap<>();

    private final Deque<LLMSession> idleSessions = new ArrayDeque<>();
    private final AtomicInteger total = new AtomicInteger();
    private final AtomicInteger borrowed = new AtomicInteger();

    public SessionPool(SessionFactory factory, SessionConfig config, int minIdle, int maxTotal, Duration maxWaitTime, EvictionPolicy evictionPolicy) {
        this(factory, config, minIdle, maxTotal, maxWaitTime, evictionPolicy, Duration.ofHours(1));
    }

    public SessionPool(SessionFactory factory, SessionConfig config, int minIdle, int maxTotal, Duration maxWaitTime, EvictionPolicy evictionPolicy, Duration sessionTtl) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.config = Objects.requireNonNull(config, "config");
        this.minIdle = minIdle;
        this.maxTotal = maxTotal;
        this.maxWaitTime = maxWaitTime != null ? maxWaitTime : Duration.ofSeconds(10);
        this.evictionPolicy = evictionPolicy != null ? evictionPolicy : EvictionPolicy.FIFO;
        this.sessionTtl = sessionTtl;

        for (int i = 0; i < minIdle; i++) {
            idleSessions.add(createSession());
            total.incrementAndGet();
        }
    }

    public synchronized LLMSession borrow() {
        pruneExpired();
        LLMSession session = fetchIdleSession();
        if (session != null) {
            borrowed.incrementAndGet();
            metadata.get(session).markBorrowed();
            return session;
        }

        if (total.get() < maxTotal) {
            LLMSession created = createSession();
            borrowed.incrementAndGet();
            return created;
        }

        long deadline = System.currentTimeMillis() + maxWaitTime.toMillis();
        while (System.currentTimeMillis() < deadline) {
            session = fetchIdleSession();
            if (session != null) {
                borrowed.incrementAndGet();
                metadata.get(session).markBorrowed();
                return session;
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
        borrowed.decrementAndGet();
        SessionMetadata data = metadata.get(session);
        if (data != null) {
            data.markReturned();
        }
        if (idleSessions.size() >= maxTotal || isExpired(session)) {
            session.close();
            metadata.remove(session);
            total.decrementAndGet();
            return;
        }
        idleSessions.offer(session);
        notifyAll();
    }

    public synchronized int getTotalSessions() {
        return total.get();
    }

    public synchronized int getBorrowedSessions() {
        return borrowed.get();
    }

    public synchronized int getIdleSessions() {
        return idleSessions.size();
    }

    private LLMSession createSession() {
        LLMSession session = factory.fromConfig(config);
        metadata.put(session, new SessionMetadata());
        total.incrementAndGet();
        return session;
    }

    private LLMSession fetchIdleSession() {
        LLMSession session = idleSessions.poll();
        while (session != null && isExpired(session)) {
            session.close();
            metadata.remove(session);
            total.decrementAndGet();
            session = idleSessions.poll();
        }
        return session;
    }

    private boolean isExpired(LLMSession session) {
        SessionMetadata data = metadata.get(session);
        if (data == null) {
            return true;
        }
        return data.getCreatedAt().plus(sessionTtl).isBefore(Instant.now());
    }

    private void pruneExpired() {
        Iterator<LLMSession> iterator = idleSessions.iterator();
        while (iterator.hasNext()) {
            LLMSession session = iterator.next();
            if (isExpired(session)) {
                iterator.remove();
                session.close();
                metadata.remove(session);
                total.decrementAndGet();
            }
        }
    }

    private static class SessionMetadata {
        private final Instant createdAt = Instant.now();
        private Instant lastBorrowedAt = Instant.now();
        private Instant lastReturnedAt = Instant.now();
        private int borrowCount;

        void markBorrowed() {
            lastBorrowedAt = Instant.now();
            borrowCount++;
        }

        void markReturned() {
            lastReturnedAt = Instant.now();
        }

        Instant getCreatedAt() {
            return createdAt;
        }
    }
}
