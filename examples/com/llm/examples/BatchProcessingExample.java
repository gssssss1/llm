package com.llm.examples;

import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.provider.ProviderType;
import com.llm.provider.common.Response;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates batch processing of multiple messages.
 */
public class BatchProcessingExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .build()) {

            List<UserMessage> questions = Arrays.asList(
                    UserMessage.of("What is 2+2?"),
                    UserMessage.of("What is the capital of Italy?"),
                    UserMessage.of("Who wrote 'Hamlet'?")
            );

            List<Response> responses = session.sendBatch(Arrays.asList(
                    questions.get(0),
                    questions.get(1),
                    questions.get(2)
            ));

            for (int i = 0; i < responses.size(); i++) {
                System.out.println("Q: " + questions.get(i).getTextContent());
                System.out.println("A: " + responses.get(i).getMessage().getTextContent());
                System.out.println();
            }
        }
    }
}
