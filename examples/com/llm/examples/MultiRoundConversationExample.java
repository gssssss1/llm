package com.llm.examples;

import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.provider.ProviderType;

/**
 * Demonstrates multi-round conversation with context.
 */
public class MultiRoundConversationExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .systemPrompt("You are a friendly assistant helping with travel planning.")
                .build()) {

            session.send(UserMessage.of("I want to visit Japan for a week. Can you suggest an itinerary?"));
            session.send(UserMessage.of("What should I pack for the trip?"));
            session.send(UserMessage.of("Recommend some local dishes to try."));
        }
    }
}
