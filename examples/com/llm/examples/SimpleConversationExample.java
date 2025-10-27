package com.llm.examples;

import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.provider.ProviderType;
import com.llm.provider.common.Response;

/**
 * Simple conversation example demonstrating basic usage.
 */
public class SimpleConversationExample {

    public static void main(String[] args) {
        // Create a session factory
        SessionFactory factory = new SessionFactory();

        // Create a session with OpenAI
        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .systemPrompt("You are a helpful assistant.")
                .build()) {

            // Send a message
            Response response = session.send(UserMessage.of("What is the capital of France?"));
            
            // Print the response
            System.out.println("Assistant: " + response.getMessage().getTextContent());

            // Continue the conversation
            Response response2 = session.send(UserMessage.of("What is its population?"));
            System.out.println("Assistant: " + response2.getMessage().getTextContent());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
