package com.llmframework.chat;

import com.llmframework.core.message.Message;
import com.llmframework.core.model.ReactiveModel;
import com.llmframework.core.model.StreamingChunk;
import com.llmframework.format.ResponseFormat;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Chat model interface
 */
public interface ChatModel extends ReactiveModel<ChatInput, ChatOutput> {
    
    /**
     * Convenience method: single message chat
     */
    default Mono<String> chat(String message) {
        return call(ChatInput.of(message))
            .map(ChatOutput::content);
    }
    
    /**
     * Convenience method: multi-turn chat
     */
    default Mono<String> chat(List<Message> messages) {
        return call(ChatInput.builder()
            .messages(messages)
            .build())
            .map(ChatOutput::content);
    }
    
    /**
     * Structured output
     */
    default <T> Mono<T> chatStructured(String message, Class<T> outputType) {
        return call(ChatInput.builder()
            .message(message)
            .responseFormat(ResponseFormat.jsonSchema(outputType))
            .build())
            .map(output -> output.parseAs(outputType));
    }
    
    /**
     * Streaming with accumulation
     */
    default Flux<StreamingChunk<ChatOutput>> streamWithAccumulation(ChatInput input) {
        AtomicReference<ChatOutput> accumulated = new AtomicReference<>();
        return stream(input)
            .map(chunk -> {
                ChatOutput current = chunk.output();
                ChatOutput prev = accumulated.get();
                ChatOutput merged = prev != null ? current.mergeWith(prev) : current;
                accumulated.set(merged);
                return new StreamingChunk<>(merged, chunk.isLast(), chunk.chunkIndex());
            });
    }
}
