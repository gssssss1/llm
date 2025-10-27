package com.llm.core.executor;

import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Executor for processing batch requests.
 */
public class BatchExecutor implements SessionExecutor {

    @Override
    public Response execute(Request request, ProviderAdapter adapter) {
        return adapter.send(request);
    }

    public List<Response> executeBatch(List<Request> requests, ProviderAdapter adapter) {
        List<Response> responses = new ArrayList<>();
        for (Request request : requests) {
            responses.add(adapter.send(request));
        }
        return responses;
    }
}
