package com.llm.examples;

import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.provider.ProviderType;

import java.util.concurrent.CompletableFuture;

/**
 * Example demonstrating asynchronous usage.
 */
public class AsyncExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .build()) {

            CompletableFuture<Void> future = session.sendAsync(UserMessage.of("Explain quantum entanglement in simple terms."))
                    .thenAccept(response -> {
                        System.out.println("Assistant: " + response.getMessage().getTextContent());
                    });

            future.join();
        }
    }
}
