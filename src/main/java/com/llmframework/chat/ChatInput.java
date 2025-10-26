package com.llmframework.chat;

import com.llmframework.core.message.Message;
import com.llmframework.core.model.ModelInput;
import com.llmframework.core.model.ModelOptions;
import com.llmframework.core.usage.TokenEstimate;
import com.llmframework.format.ResponseFormat;
import com.llmframework.tool.Tool;
import com.llmframework.tool.ToolChoice;

import java.util.*;
import java.util.function.Consumer;

/**
 * Chat model input
 */
public record ChatInput(
    List<Message> messages,
    ModelOptions options,
    List<Tool> tools,
    ToolChoice toolChoice,
    ResponseFormat responseFormat,
    String requestId,
    Map<String, String> metadata
) implements ModelInput {
    
    public ChatInput {
        messages = List.copyOf(messages);
        tools = tools == null ? List.of() : List.copyOf(tools);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        requestId = requestId == null ? UUID.randomUUID().toString() : requestId;
    }
    
    public static ChatInput of(String message) {
        return builder().message(Message.user(message)).build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public TokenEstimate estimateTokens() {
        int promptTokens = messages.stream()
            .mapToInt(Message::estimateTokens)
            .sum();
        return new TokenEstimate(promptTokens, options.maxTokens());
    }
    
    public static class Builder {
        private List<Message> messages = new ArrayList<>();
        private ModelOptions options = ModelOptions.defaults();
        private List<Tool> tools = new ArrayList<>();
        private ToolChoice toolChoice = ToolChoice.auto();
        private ResponseFormat responseFormat = ResponseFormat.text();
        private String requestId;
        private Map<String, String> metadata = new HashMap<>();
        
        public Builder message(Message message) {
            this.messages.add(message);
            return this;
        }
        
        public Builder message(String content) {
            return message(Message.user(content));
        }
        
        public Builder messages(List<Message> messages) {
            this.messages.addAll(messages);
            return this;
        }
        
        public Builder system(String content) {
            return message(Message.system(content));
        }
        
        public Builder user(String content) {
            return message(Message.user(content));
        }
        
        public Builder assistant(String content) {
            return message(Message.assistant(content));
        }
        
        public Builder options(ModelOptions options) {
            this.options = options;
            return this;
        }
        
        public Builder options(Consumer<ModelOptions.Builder> configurator) {
            ModelOptions.Builder builder = ModelOptions.builder();
            configurator.accept(builder);
            this.options = builder.build();
            return this;
        }
        
        public Builder tool(Tool tool) {
            this.tools.add(tool);
            return this;
        }
        
        public Builder tools(List<Tool> tools) {
            this.tools.addAll(tools);
            return this;
        }
        
        public Builder toolChoice(ToolChoice toolChoice) {
            this.toolChoice = toolChoice;
            return this;
        }
        
        public Builder responseFormat(ResponseFormat responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }
        
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public Builder metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public ChatInput build() {
            return new ChatInput(
                messages, options, tools, toolChoice,
                responseFormat, requestId, metadata
            );
        }
    }
}
