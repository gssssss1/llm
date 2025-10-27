package com.llm.core.executor;

import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;

import java.util.function.Consumer;

/**
 * Executor for streaming responses.
 */
public class StreamExecutor implements SessionExecutor {

    @Override
    public Response execute(Request request, ProviderAdapter adapter) {
        return adapter.send(request);
    }

    /**
     * Executes request with streaming response.
     */
    public void executeStream(Request request, ProviderAdapter adapter, Consumer<String> onChunk) {
        adapter.sendStream(request, onChunk);
    }
}
