package com.llm.examples;

import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.provider.ProviderType;

/**
 * Example of streaming responses.
 */
public class StreamingExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .build()) {

            System.out.print("Assistant: ");
            session.sendStream(UserMessage.of("Write a short poem about the ocean."), chunk -> {
                System.out.print(chunk);
            });
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
