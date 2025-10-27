package com.llm.core.session;

import com.llm.config.ModelConfig;
import com.llm.core.interceptor.InterceptorChain;
import com.llm.core.message.AssistantMessage;
import com.llm.core.message.UserMessage;
import com.llm.persistence.MemoryStore;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.ProviderType;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DefaultLLMSessionTest {

    private ProviderAdapter adapter;
    private SessionConfig config;
    private SessionContext context;
    private DefaultLLMSession session;

    @BeforeEach
    void setUp() {
        adapter = Mockito.mock(ProviderAdapter.class);
        when(adapter.getType()).thenReturn(ProviderType.OPENAI);

        ModelConfig modelConfig = ModelConfig.builder()
                .provider("openai")
                .model("gpt-3.5-turbo")
                .build();

        config = SessionConfig.builder()
                .modelConfig(modelConfig)
                .build();

        context = SessionContext.builder()
                .history(new FullMessageHistory())
                .build();

        Response response = Response.builder()
                .message(AssistantMessage.of("Hello"))
                .build();
        when(adapter.send(any(Request.class))).thenReturn(response);

        session = new DefaultLLMSession("session-1", config, context, adapter, InterceptorChain.builder().build(), new MemoryStore());
    }

    @Test
    void testSend() {
        Response response = session.send(UserMessage.of("Hi"));
        assertEquals("Hello", response.getMessage().getTextContent());
        assertEquals(SessionState.IDLE, session.getState());
    }

    @Test
    void testAsync() {
        Response response = session.sendAsync(UserMessage.of("Async"))
                .join();
        assertEquals("Hello", response.getMessage().getTextContent());
    }

    @Test
    void testHistory() {
        session.send(UserMessage.of("Q1"));
        assertEquals(2, session.getHistory().size());
        session.clearHistory();
        assertEquals(0, session.getHistory().size());
    }

    @Test
    void testFork() {
        session.send(UserMessage.of("Q1"));
        LLMSession fork = session.fork();
        assertNotSame(session, fork);
        assertEquals(session.getHistory().size(), fork.getHistory().size());
    }
}
