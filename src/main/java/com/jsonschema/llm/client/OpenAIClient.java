package com.jsonschema.llm.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jsonschema.llm.client.model.ChatRequest;
import com.jsonschema.llm.client.model.ChatResponse;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OpenAIClient implements LLMClient {
    
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    
    public OpenAIClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }
    
    public OpenAIClient(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public OpenAIClient(String apiKey, String baseUrl, OkHttpClient httpClient) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
    }
    
    @Override
    public ChatResponse chat(ChatRequest request) throws IOException {
        String url = baseUrl + "/chat/completions";
        
        JsonObject requestJson = request.toOpenAIJson();
        RequestBody body = RequestBody.create(requestJson.toString(), JSON);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("OpenAI API call failed: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            
            return ChatResponse.fromOpenAIJson(responseJson);
        }
    }
    
    @Override
    public void chatStream(ChatRequest request, StreamCallback callback) throws IOException {
        String url = baseUrl + "/chat/completions";
        
        JsonObject requestJson = request.toOpenAIJson();
        requestJson.addProperty("stream", true);
        
        RequestBody body = RequestBody.create(requestJson.toString(), JSON);
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                callback.onError(new IOException("OpenAI API call failed: " + response.code() + " - " + errorBody));
                return;
            }
            
            callback.onStart();
            
            StringBuilder fullContent = new StringBuilder();
            
            try {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    callback.onError(new IOException("Empty response body"));
                    return;
                }
                
                String line;
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(responseBody.byteStream())
                );
                
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        
                        if ("[DONE]".equals(data)) {
                            callback.onComplete(fullContent.toString());
                            break;
                        }
                        
                        try {
                            JsonObject chunk = JsonParser.parseString(data).getAsJsonObject();
                            
                            if (chunk.has("choices") && chunk.getAsJsonArray("choices").size() > 0) {
                                JsonObject choice = chunk.getAsJsonArray("choices").get(0).getAsJsonObject();
                                
                                if (choice.has("delta")) {
                                    JsonObject delta = choice.getAsJsonObject("delta");
                                    
                                    if (delta.has("content") && !delta.get("content").isJsonNull()) {
                                        String content = delta.get("content").getAsString();
                                        fullContent.append(content);
                                        callback.onChunk(content);
                                    }
                                }
                                
                                if (choice.has("finish_reason") && !choice.get("finish_reason").isJsonNull()) {
                                    callback.onComplete(fullContent.toString());
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            // Skip malformed chunks
                        }
                    }
                }
            } catch (Exception e) {
                callback.onError(e);
            }
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
        
        public OpenAIClient build() {
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
            
            return new OpenAIClient(apiKey, baseUrl, httpClient);
        }
    }
}
