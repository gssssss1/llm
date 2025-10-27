package com.llm.examples;

import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.provider.ProviderType;

/**
 * Demonstrates saving and restoring session state.
 */
public class PersistenceExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();

        // Create and use a session
        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .build()) {

            session.send(UserMessage.of("Tell me about the Roman Empire."));
            session.send(UserMessage.of("Who was Julius Caesar?"));

            // Save the session state
            session.save("session-snapshot.json");
            System.out.println("Session saved.");
        }

        // Later, restore the session
        try (LLMSession restored = factory.restore("session-snapshot.json")) {
            System.out.println("Session restored with " + restored.getHistory().size() + " messages.");
            
            // Continue the conversation
            restored.send(UserMessage.of("What happened after his assassination?"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
