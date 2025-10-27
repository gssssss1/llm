package com.llm.examples;

import com.llm.core.message.MultiModalContent;
import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.provider.ProviderType;

import java.util.Arrays;

/**
 * Demonstrates sending multi-modal content (text + image link).
 */
public class MultiModalExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-4o")
                .build()) {

            UserMessage message = UserMessage.builder()
                    .text("Describe the image and write a caption.")
                    .addContent(MultiModalContent.image("https://example.com/image.jpg"))
                    .build();

            session.send(message);
        }
    }
}
