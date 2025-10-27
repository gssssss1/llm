package com.llm.examples;

import com.llm.config.ModelConfig;
import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionConfig;
import com.llm.core.session.SessionFactory;
import com.llm.core.session.SessionPool;
import com.llm.provider.ProviderType;

import java.time.Duration;

/**
 * Demonstrates session pooling with borrowing and releasing sessions.
 */
public class SessionPoolExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();
        SessionConfig config = SessionConfig.builder()
                .modelConfig(
                        ModelConfig.builder()
                                .provider(ProviderType.OPENAI.getId())
                                .model("gpt-3.5-turbo")
                                .build()
                )
                .build();

        SessionPool pool = new SessionPool(
                factory,
                config,
                2,                              // min idle
                5,                              // max total
                Duration.ofSeconds(5),
                SessionPool.EvictionPolicy.LRU,
                Duration.ofMinutes(30)
        );

        LLMSession session = pool.borrow();
        try {
            session.send(UserMessage.of("Explain the benefits of session pooling."));
        } finally {
            pool.release(session);
        }

        System.out.println("Total sessions: " + pool.getTotalSessions());
        System.out.println("Idle sessions: " + pool.getIdleSessions());
        System.out.println("Borrowed sessions: " + pool.getBorrowedSessions());
    }
}
