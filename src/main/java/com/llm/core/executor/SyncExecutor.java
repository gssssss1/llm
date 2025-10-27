package com.llm.core.executor;

import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;

/**
 * Synchronous executor for LLM requests.
 */
public class SyncExecutor implements SessionExecutor {

    @Override
    public Response execute(Request request, ProviderAdapter adapter) {
        return adapter.send(request);
    }
}
