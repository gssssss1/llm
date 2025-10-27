package com.llm.transport;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Parses Server-Sent Events (SSE) streams.
 */
public class SSEParser {

    private final OkHttpClient client;

    public SSEParser(OkHttpClient client) {
        this.client = client;
    }

    public CompletableFuture<Void> openStream(String url, Map<String, String> headers, Consumer<String> onEvent, Consumer<Throwable> onError) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        EventSources.createFactory(client).newEventSource(requestBuilder.build(), new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                if (data != null) {
                    onEvent.accept(data);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                future.complete(null);
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                future.completeExceptionally(t);
                if (onError != null) {
                    onError.accept(t);
                }
            }
        });

        return future;
    }
}
