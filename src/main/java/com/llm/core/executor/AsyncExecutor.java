package com.llm.core.executor;

import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asynchronous executor based on {@link CompletableFuture}.
 */
public class AsyncExecutor implements SessionExecutor {

    private final ExecutorService executorService;

    public AsyncExecutor() {
        this(Executors.newCachedThreadPool());
    }

    public AsyncExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public Response execute(Request request, ProviderAdapter adapter) {
        return adapter.send(request);
    }

    /**
     * Executes request asynchronously.
     */
    public CompletableFuture<Response> executeAsync(Request request, ProviderAdapter adapter) {
        return CompletableFuture.supplyAsync(() -> adapter.send(request), executorService);
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}
