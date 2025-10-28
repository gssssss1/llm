package com.jsonschema.llm.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jsonschema.llm.client.model.ChatRequest;
import com.jsonschema.llm.client.model.ChatResponse;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AnthropicClient implements LLMClient {
    
    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com/v1";
    private static final String DEFAULT_VERSION = "2023-06-01";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final String version;
    
    public AnthropicClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL, DEFAULT_VERSION);
    }
    
    public AnthropicClient(String apiKey, String baseUrl, String version) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.version = version;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public AnthropicClient(String apiKey, String baseUrl, String version, OkHttpClient httpClient) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.version = version;
        this.httpClient = httpClient;
    }
    
    @Override
    public ChatResponse chat(ChatRequest request) throws IOException {
        String url = baseUrl + "/messages";
        
        JsonObject requestJson = request.toAnthropicJson();
        RequestBody body = RequestBody.create(requestJson.toString(), JSON);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", version)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Anthropic API call failed: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            
            return ChatResponse.fromAnthropicJson(responseJson);
        }
    }
    
    @Override
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private String version = DEFAULT_VERSION;
        private OkHttpClient httpClient;
        private long connectTimeout = 30;
        private long readTimeout = 60;
        private long writeTimeout = 30;
        
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }
        
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder httpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }
        
        public Builder connectTimeout(long seconds) {
            this.connectTimeout = seconds;
            return this;
        }
        
        public Builder readTimeout(long seconds) {
            this.readTimeout = seconds;
            return this;
        }
        
        public Builder writeTimeout(long seconds) {
            this.writeTimeout = seconds;
            return this;
        }
        
        public AnthropicClient build() {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("API key is required");
            }
            
            if (httpClient == null) {
                httpClient = new OkHttpClient.Builder()
                        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                        .readTimeout(readTimeout, TimeUnit.SECONDS)
                        .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                        .build();
            }
            
            return new AnthropicClient(apiKey, baseUrl, version, httpClient);
        }
    }
}
