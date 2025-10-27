package com.llm.core.interceptor.impl;

import com.llm.core.interceptor.Interceptor;
import com.llm.exception.LLMException;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles errors and wraps them into a consistent exception hierarchy.
 */
public class ErrorHandlerInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlerInterceptor.class);

    @Override
    public Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        try {
            return chain.proceed(request);
        } catch (Exception e) {
            logger.error("Provider call failed", e);
            throw new LLMException("Provider call failed: " + e.getMessage(), e);
        }
    }
}
