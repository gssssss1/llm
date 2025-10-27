package com.llm.core.interceptor.impl;

import com.llm.core.interceptor.Interceptor;
import com.llm.core.message.AbstractMessage;
import com.llm.core.message.Message;
import com.llm.exception.LLMException;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import com.llm.security.ContentFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters content for inappropriate material.
 */
public class ContentFilterInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(ContentFilterInterceptor.class);

    private final ContentFilter contentFilter;
    private final boolean blockViolations;

    public ContentFilterInterceptor(ContentFilter contentFilter, boolean blockViolations) {
        this.contentFilter = contentFilter;
        this.blockViolations = blockViolations;
    }

    @Override
    public Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        for (Message message : request.getMessages()) {
            if (message instanceof AbstractMessage) {
                String content = ((AbstractMessage) message).getTextContent();
                if (contentFilter.isViolation(content)) {
                    logger.warn("Content policy violation detected");
                    if (blockViolations) {
                        throw new LLMException("Content policy violation");
                    }
                }
            }
        }

        Response response = chain.proceed(request);

        String responseText = response.getMessage().getTextContent();
        if (contentFilter.isViolation(responseText)) {
            logger.warn("Response contains policy violation");
        }

        return response;
    }
}
