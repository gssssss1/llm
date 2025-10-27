package com.llm.core.interceptor.impl;

import com.llm.core.interceptor.Interceptor;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs request and response information.
 */
public class LoggingInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public Response intercept(Request request, Chain chain, ProviderAdapter adapter) {
        logger.info("Sending request with {} messages to provider {}", 
                   request.getMessages().size(), 
                   request.getConfig().getModelConfig().getProvider());
        
        long start = System.currentTimeMillis();
        Response response = chain.proceed(request);
        long duration = System.currentTimeMillis() - start;
        
        logger.info("Received response in {}ms, finish reason: {}", duration, response.getFinishReason());
        if (response.getUsage() != null) {
            logger.info("Token usage - prompt: {}, completion: {}, total: {}",
                       response.getUsage().getPromptTokens(),
                       response.getUsage().getCompletionTokens(),
                       response.getUsage().getTotalTokens());
        }
        
        return response;
    }
}
