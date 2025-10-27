package com.llm.provider;

import com.llm.provider.common.Request;
import com.llm.provider.common.Response;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Unified adapter interface for different LLM providers.
 */
public interface ProviderAdapter {

    /**
     * Returns provider type identifier.
     */
    ProviderType getType();

    /**
     * Sends a synchronous request.
     */
    Response send(Request request);

    /**
     * Sends a request and streams the response.
     */
    void sendStream(Request request, Consumer<String> onChunk);

    /**
     * Sends multiple requests in batch.
     */
    default void sendBatch(Request request, Consumer<Response> onResponse) {
        onResponse.accept(send(request));
    }

    /**
     * Checks if the provider supports a feature.
     */
    boolean supportsFeature(String feature);

    /**
     * Estimates token usage for messages.
     */
    int estimateTokens(Request request);

    /**
     * Returns allowed models for this adapter.
     */
    Set<String> supportedModels();
}
