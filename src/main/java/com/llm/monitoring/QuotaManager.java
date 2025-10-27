package com.llm.monitoring;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages quotas for API usage.
 */
public class QuotaManager {

    private final Map<String, Quota> quotas = new ConcurrentHashMap<>();

    public void setQuota(String provider, int maxTokensPerMinute) {
        quotas.put(provider, new Quota(maxTokensPerMinute));
    }

    public synchronized boolean tryConsume(String provider, int tokens) {
        Quota quota = quotas.computeIfAbsent(provider, p -> new Quota(Integer.MAX_VALUE));
        return quota.tryConsume(tokens);
    }

    private static class Quota {
        private final int maxTokensPerMinute;
        private int tokensUsed;
        private Instant windowStart;

        private Quota(int maxTokensPerMinute) {
            this.maxTokensPerMinute = maxTokensPerMinute;
            this.tokensUsed = 0;
            this.windowStart = Instant.now();
        }

        private boolean tryConsume(int tokens) {
            Instant now = Instant.now();
            if (now.isAfter(windowStart.plusSeconds(60))) {
                tokensUsed = 0;
                windowStart = now;
            }
            if (tokensUsed + tokens > maxTokensPerMinute) {
                return false;
            }
            tokensUsed += tokens;
            return true;
        }
    }
}
