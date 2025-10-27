package com.llm.examples;

import com.llm.core.interceptor.InterceptorChain;
import com.llm.core.interceptor.impl.LoggingInterceptor;
import com.llm.core.message.UserMessage;
import com.llm.core.session.LLMSession;
import com.llm.core.session.SessionFactory;
import com.llm.provider.ProviderType;

/**
 * Demonstrates using interceptors for cross-cutting concerns.
 */
public class InterceptorExample {

    public static void main(String[] args) {
        SessionFactory factory = new SessionFactory();

        InterceptorChain chain = InterceptorChain.builder()
                .addInterceptor(new LoggingInterceptor())
                .build();

        try (LLMSession session = factory.builder()
                .provider(ProviderType.OPENAI)
                .model("gpt-3.5-turbo")
                .interceptorChain(chain)
                .build()) {

            session.send(UserMessage.of("What is machine learning?"));
        }
    }
}
