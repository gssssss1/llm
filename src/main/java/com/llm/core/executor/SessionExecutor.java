package com.llm.core.executor;

import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;

/**
 * Base interface for session executors.
 */
public interface SessionExecutor {

    /**
     * Executes the request synchronously using the provided adapter.
     */
    Response execute(Request request, ProviderAdapter adapter);
}
