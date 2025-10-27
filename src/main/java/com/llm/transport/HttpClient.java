package com.llm.transport;

import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * HTTP client wrapper for LLM API communication.
 */
public class HttpClient {

    private final OkHttpClient client;

    public HttpClient() {
        this(Duration.ofSeconds(60));
    }

    public HttpClient(Duration timeout) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .connectionPool(new ConnectionPool())
                .build();
    }

    public HttpClient(OkHttpClient client) {
        this.client = client;
    }

    public String post(String url, String jsonBody, Map<String, String> headers) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);

        if (headers != null) {
            headers.forEach(builder::addHeader);
        }

        try (Response response = client.newCall(builder.build()).execute()) {
            ResponseBody responseBody = response.body();
            if (!response.isSuccessful()) {
                String errorBody = responseBody != null ? responseBody.string() : "Unknown error";
                throw new IOException("HTTP error " + response.code() + ": " + errorBody);
            }
            return responseBody != null ? responseBody.string() : "";
        }
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
