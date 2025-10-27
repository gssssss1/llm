package com.llm.core.interceptor.impl;

import com.llm.core.interceptor.Interceptor;
import com.llm.core.message.Message;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches responses based on request content to avoid redundant API calls.
 */
public class CachingInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(CachingInterceptor.class);

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final int maxSize;

    public CachingInterceptor() {
        this(Duration.ofMinutes(15), 1000);
    }

    public CachingInterceptor(Duration ttl, int maxSize) {
        this.ttl = ttl;
        this.maxSize = maxSize;
    }

    @Override
    public Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        String key = generateCacheKey(request.getMessages());
        
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            logger.debug("Cache hit for key: {}", key.substring(0, Math.min(8, key.length())));
            return entry.response;
        }

        logger.debug("Cache miss, executing request");
        Response response = chain.proceed(request);

        if (cache.size() >= maxSize) {
            evictOldest();
        }

        cache.put(key, new CacheEntry(response, Instant.now().plus(ttl)));
        return response;
    }

    private String generateCacheKey(List<Message> messages) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Message msg : messages) {
                digest.update(msg.toString().getBytes());
            }
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            return messages.toString();
        }
    }

    private void evictOldest() {
        String oldestKey = null;
        Instant oldestTime = Instant.now();
        
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().createdAt.isBefore(oldestTime)) {
                oldestTime = entry.getValue().createdAt;
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cache.remove(oldestKey);
        }
    }

    public void clearCache() {
        cache.clear();
        logger.info("Cache cleared");
    }

    public int getCacheSize() {
        return cache.size();
    }

    private static class CacheEntry {
        final Response response;
        final Instant createdAt;
        final Instant expiresAt;

        CacheEntry(Response response, Instant expiresAt) {
            this.response = response;
            this.createdAt = Instant.now();
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
