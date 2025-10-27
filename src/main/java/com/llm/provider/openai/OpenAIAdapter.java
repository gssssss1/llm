package com.llm.provider.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.llm.core.message.*;
import com.llm.core.tool.ToolCall;
import com.llm.exception.AuthenticationException;
import com.llm.exception.ProviderException;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.ProviderType;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import com.llm.transport.HttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Provider adapter for OpenAI.
 */
public class OpenAIAdapter implements ProviderAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIAdapter.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final Set<String> SUPPORTED_MODELS = new HashSet<>(Arrays.asList(
            "gpt-3.5-turbo", "gpt-3.5-turbo-16k", "gpt-4", "gpt-4-32k", "gpt-4-turbo", "gpt-4o", "gpt-4o-mini"
    ));

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAIAdapter(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "OpenAI API key is required");
        this.httpClient = new HttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ProviderType getType() {
        return ProviderType.OPENAI;
    }

    @Override
    public Response send(Request request) {
        try {
            String jsonBody = buildRequestBody(request);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey);
            headers.put("Content-Type", "application/json");

            String responseBody = httpClient.post(API_URL, jsonBody, headers);
            return parseResponse(responseBody);
        } catch (IOException e) {
            if (e.getMessage().contains("401")) {
                throw new AuthenticationException("Invalid OpenAI API key");
            }
            throw new ProviderException("OpenAI API call failed", e);
        }
    }

    @Override
    public void sendStream(Request request, Consumer<String> onChunk) {
        try {
            String jsonBody = buildRequestBody(request, true);
            
            okhttp3.Request okRequest = new okhttp3.Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            EventSources.createFactory(httpClient.getClient()).newEventSource(okRequest, new EventSourceListener() {
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    if (data.equals("[DONE]")) {
                        return;
                    }
                    try {
                        JsonNode json = objectMapper.readTree(data);
                        JsonNode choices = json.get("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null && delta.has("content")) {
                                String content = delta.get("content").asText();
                                onChunk.accept(content);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing SSE data", e);
                    }
                }

                @Override
                public void onFailure(EventSource eventSource, Throwable t, okhttp3.Response response) {
                    logger.error("Stream failed", t);
                }
            });
        } catch (Exception e) {
            throw new ProviderException("Failed to stream from OpenAI", e);
        }
    }

    @Override
    public boolean supportsFeature(String feature) {
        return "function_calling".equals(feature) || "streaming".equals(feature);
    }

    @Override
    public int estimateTokens(Request request) {
        // Rough estimation: ~4 characters per token
        int total = 0;
        for (Message msg : request.getMessages()) {
            if (msg instanceof AbstractMessage) {
                total += ((AbstractMessage) msg).getTextContent().length() / 4;
            }
        }
        return total;
    }

    @Override
    public Set<String> supportedModels() {
        return SUPPORTED_MODELS;
    }

    private String buildRequestBody(Request request) {
        return buildRequestBody(request, false);
    }

    private String buildRequestBody(Request request, boolean stream) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", request.getConfig().getModelConfig().getModel());
        root.put("stream", stream);

        ArrayNode messages = root.putArray("messages");
        for (Message message : request.getMessages()) {
            ObjectNode msgNode = messages.addObject();
            msgNode.put("role", getRoleName(message.getType()));
            if (message instanceof AbstractMessage) {
                msgNode.put("content", ((AbstractMessage) message).getTextContent());
            }
        }

        // Add parameters
        Map<String, Object> params = request.getConfig().getModelConfig().getParameters();
        params.forEach((key, value) -> {
            if (value instanceof Number) {
                msgNode.put(key, ((Number) value).doubleValue());
            } else if (value instanceof String) {
                root.put(key, (String) value);
            }
        });

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new ProviderException("Failed to build request", e);
        }
    }

    private Response parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            if (choices == null || choices.size() == 0) {
                throw new ProviderException("No choices in response");
            }

            JsonNode choice = choices.get(0);
            JsonNode messageNode = choice.get("message");
            String content = messageNode.get("content") != null ? messageNode.get("content").asText() : "";
            String finishReason = choice.has("finish_reason") ? choice.get("finish_reason").asText() : null;

            AssistantMessage message = AssistantMessage.of(content, finishReason);

            List<ToolCall> toolCalls = new ArrayList<>();
            if (messageNode.has("tool_calls")) {
                JsonNode toolCallsNode = messageNode.get("tool_calls");
                for (JsonNode toolCallNode : toolCallsNode) {
                    String id = toolCallNode.get("id").asText();
                    String name = toolCallNode.get("function").get("name").asText();
                    String argsJson = toolCallNode.get("function").get("arguments").asText();
                    Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);
                    toolCalls.add(new ToolCall(id, name, args));
                }
            }

            Response.Usage usage = null;
            if (root.has("usage")) {
                JsonNode usageNode = root.get("usage");
                usage = new Response.Usage(
                        usageNode.get("prompt_tokens").asInt(),
                        usageNode.get("completion_tokens").asInt(),
                        usageNode.get("total_tokens").asInt()
                );
            }

            return Response.builder()
                    .message(message)
                    .toolCalls(toolCalls)
                    .usage(usage)
                    .finishReason(finishReason)
                    .build();
        } catch (Exception e) {
            throw new ProviderException("Failed to parse response", e);
        }
    }

    private String getRoleName(MessageType type) {
        switch (type) {
            case SYSTEM: return "system";
            case USER: return "user";
            case ASSISTANT: return "assistant";
            case TOOL: return "tool";
            default: return "user";
        }
    }
}
