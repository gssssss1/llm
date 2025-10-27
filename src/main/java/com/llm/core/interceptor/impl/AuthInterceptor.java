package com.llm.core.interceptor.impl;

import com.llm.core.interceptor.Interceptor;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ensures API keys or tokens are present before invocation.
 */
public class AuthInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    private final Map<String, String> providerKeys = new ConcurrentHashMap<>();

    public void registerKey(String provider, String apiKey) {
        providerKeys.put(provider, apiKey);
    }

    @Override
    public Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        String provider = adapter.getType().getId();
        if (!providerKeys.containsKey(provider)) {
            throw new IllegalStateException("Missing API key for provider: " + provider);
        }
        logger.debug("Authenticated request for provider {}", provider);
        return chain.proceed(request);
    }
}
