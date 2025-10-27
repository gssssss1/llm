package com.llm.provider.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.llm.core.message.AbstractMessage;
import com.llm.core.message.AssistantMessage;
import com.llm.core.message.Message;
import com.llm.core.message.MessageType;
import com.llm.core.tool.ToolCall;
import com.llm.exception.AuthenticationException;
import com.llm.exception.ProviderException;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.ProviderType;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import com.llm.transport.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Adapter for Anthropic Claude models.
 */
public class AnthropicAdapter implements ProviderAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AnthropicAdapter.class);
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final Set<String> SUPPORTED_MODELS = new HashSet<>(Arrays.asList(
            "claude-2.1", "claude-2.0", "claude-instant-1.2", "claude-3-sonnet", "claude-3-opus"
    ));

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AnthropicAdapter(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "Anthropic API key is required");
        this.httpClient = new HttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ProviderType getType() {
        return ProviderType.ANTHROPIC;
    }

    @Override
    public Response send(Request request) {
        try {
            String body = buildRequestBody(request);
            Map<String, String> headers = new HashMap<>();
            headers.put("x-api-key", apiKey);
            headers.put("anthropic-version", "2023-06-01");
            headers.put("Content-Type", "application/json");

            String responseBody = httpClient.post(API_URL, body, headers);
            return parseResponse(responseBody);
        } catch (IOException e) {
            if (e.getMessage().contains("401")) {
                throw new AuthenticationException("Invalid Anthropic API key");
            }
            throw new ProviderException("Anthropic API call failed", e);
        }
    }

    @Override
    public void sendStream(Request request, Consumer<String> onChunk) {
        // Anthropic streaming requires SSE - omitted for brevity
        logger.warn("Streaming not implemented for Anthropic adapter");
    }

    @Override
    public boolean supportsFeature(String feature) {
        return "tool_use".equals(feature);
    }

    @Override
    public int estimateTokens(Request request) {
        int total = 0;
        for (Message msg : request.getMessages()) {
            if (msg instanceof AbstractMessage) {
                total += ((AbstractMessage) msg).getTextContent().length() / 3;
            }
        }
        return total;
    }

    @Override
    public Set<String> supportedModels() {
        return SUPPORTED_MODELS;
    }

    private String buildRequestBody(Request request) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", request.getConfig().getModelConfig().getModel());
        root.put("max_tokens", 1024);

        ArrayNode messagesNode = root.putArray("messages");
        for (Message message : request.getMessages()) {
            ObjectNode messageNode = messagesNode.addObject();
            messageNode.put("role", getRoleName(message.getType()));
            if (message instanceof AbstractMessage) {
                messageNode.put("content", ((AbstractMessage) message).getTextContent());
            }
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new ProviderException("Failed to build Anthropic request", e);
        }
    }

    private Response parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            ArrayNode contentArray = (ArrayNode) root.get("content");
            StringBuilder sb = new StringBuilder();
            if (contentArray != null) {
                contentArray.forEach(node -> sb.append(node.get("text").asText()));
            }

            AssistantMessage message = AssistantMessage.of(sb.toString());

            List<ToolCall> toolCalls = new ArrayList<>();
            if (root.has("tool_calls")) {
                root.get("tool_calls").forEach(callNode -> {
                    String id = callNode.get("id").asText();
                    String name = callNode.get("name").asText();
                    Map<String, Object> args = objectMapper.convertValue(callNode.get("arguments"), Map.class);
                    toolCalls.add(new ToolCall(id, name, args));
                });
            }

            Response.Usage usage = null;
            if (root.has("usage")) {
                JsonNode usageNode = root.get("usage");
                usage = new Response.Usage(
                        usageNode.get("input_tokens").asInt(),
                        usageNode.get("output_tokens").asInt(),
                        usageNode.get("total_tokens").asInt()
                );
            }

            return Response.builder()
                    .message(message)
                    .toolCalls(toolCalls)
                    .usage(usage)
                    .build();
        } catch (Exception e) {
            throw new ProviderException("Failed to parse Anthropic response", e);
        }
    }

    private String getRoleName(MessageType type) {
        switch (type) {
            case SYSTEM: return "system";
            case USER: return "user";
            case ASSISTANT: return "assistant";
            default: return "user";
        }
    }
}
