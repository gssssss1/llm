package com.llmframework.chat;

import com.llmframework.core.message.Message;
import com.llmframework.core.model.ModelOutput;
import com.llmframework.core.usage.Usage;
import com.llmframework.tool.ToolCall;
import com.llmframework.util.JsonUtils;

import java.time.Duration;
import java.util.List;

/**
 * Chat model output
 */
public record ChatOutput(
    String requestId,
    Message message,
    List<ToolCall> toolCalls,
    FinishReason finishReason,
    Usage usage,
    String modelVersion,
    Duration duration,
    boolean fromCache
) implements ModelOutput {
    
    public String content() {
        return message.content();
    }
    
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
    
    public <T> T parseAs(Class<T> type) {
        return JsonUtils.parse(content(), type);
    }
    
    public ChatOutput mergeWith(ChatOutput previous) {
        String mergedContent = previous.content() + content();
        Message mergedMessage = Message.assistant(mergedContent);
        
        return new ChatOutput(
            requestId,
            mergedMessage,
            toolCalls,
            finishReason,
            usage,
            modelVersion,
            duration,
            fromCache
        );
    }
}
