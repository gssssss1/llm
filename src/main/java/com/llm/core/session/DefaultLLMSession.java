package com.llm.core.session;

import com.llm.core.executor.AsyncExecutor;
import com.llm.core.executor.StreamExecutor;
import com.llm.core.executor.SyncExecutor;
import com.llm.core.interceptor.InterceptorChain;
import com.llm.core.message.Message;
import com.llm.exception.LLMException;
import com.llm.persistence.SessionPersistence;
import com.llm.persistence.SessionSnapshot;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.common.Request;
import com.llm.provider.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Default implementation of LLMSession.
 */
public class DefaultLLMSession implements LLMSession {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLLMSession.class);

    private final String sessionId;
    private final SessionConfig config;
    private final SessionContext context;
    private final ProviderAdapter provider;
    private final InterceptorChain interceptorChain;
    private final SyncExecutor syncExecutor;
    private final AsyncExecutor asyncExecutor;
    private final StreamExecutor streamExecutor;
    private final AtomicReference<SessionState> state;
    private final SessionPersistence persistence;

    public DefaultLLMSession(String sessionId,
                            SessionConfig config,
                            SessionContext context,
                            ProviderAdapter provider,
                            InterceptorChain interceptorChain,
                            SessionPersistence persistence) {
        this.sessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();
        this.config = config;
        this.context = context;
        this.provider = provider;
        this.interceptorChain = interceptorChain;
        this.syncExecutor = new SyncExecutor();
        this.asyncExecutor = new AsyncExecutor();
        this.streamExecutor = new StreamExecutor();
        this.state = new AtomicReference<>(SessionState.IDLE);
        this.persistence = persistence;
        logger.info("Created new session: {}", this.sessionId);
    }

    @Override
    public Response send(Message message) {
        return send(Collections.singletonList(message));
    }

    @Override
    public Response send(List<Message> messages) {
        ensureNotClosed();
        transitionTo(SessionState.RUNNING);

        try {
            context.getHistory().addAll(messages);

            Request request = Request.builder()
                    .messages(context.getHistory().getMessages())
                    .config(config)
                    .context(context)
                    .build();

            Response response = interceptorChain.intercept(request, provider);
            context.getHistory().add(response.getMessage());

            return response;
        } finally {
            transitionTo(SessionState.IDLE);
        }
    }

    @Override
    public CompletableFuture<Response> sendAsync(Message message) {
        ensureNotClosed();
        transitionTo(SessionState.RUNNING);

        context.getHistory().add(message);

        Request request = Request.builder()
                .messages(context.getHistory().getMessages())
                .config(config)
                .context(context)
                .build();

        return asyncExecutor.executeAsync(request, provider)
                .thenApply(response -> {
                    context.getHistory().add(response.getMessage());
                    transitionTo(SessionState.IDLE);
                    return response;
                })
                .exceptionally(ex -> {
                    transitionTo(SessionState.IDLE);
                    throw new LLMException("Async send failed", ex);
                });
    }

    @Override
    public void sendStream(Message message, Consumer<String> onChunk) {
        ensureNotClosed();
        transitionTo(SessionState.RUNNING);

        try {
            context.getHistory().add(message);

            Request request = Request.builder()
                    .messages(context.getHistory().getMessages())
                    .config(config)
                    .context(context)
                    .streaming(true)
                    .build();

            streamExecutor.executeStream(request, provider, onChunk);
        } finally {
            transitionTo(SessionState.IDLE);
        }
    }

    @Override
    public List<Response> sendBatch(List<Message> messages) {
        List<Response> responses = new ArrayList<>();
        for (Message message : messages) {
            responses.add(send(message));
        }
        return responses;
    }

    @Override
    public SessionState getState() {
        return state.get();
    }

    @Override
    public SessionConfig getConfig() {
        return config;
    }

    @Override
    public SessionContext getContext() {
        return context;
    }

    @Override
    public List<Message> getHistory() {
        return context.getHistory().getMessages();
    }

    @Override
    public void clearHistory() {
        context.getHistory().clear();
        logger.info("Cleared history for session {}", sessionId);
    }

    @Override
    public void rollback(int steps) {
        context.getHistory().rollback(steps);
        logger.info("Rolled back {} steps in session {}", steps, sessionId);
    }

    @Override
    public void reset() {
        clearHistory();
        context.getVariables().clear();
        logger.info("Reset session {}", sessionId);
    }

    @Override
    public LLMSession fork() {
        SessionContext newContext = SessionContext.builder()
                .history(context.getHistory().copy())
                .systemPrompt(context.getSystemPrompt().orElse(null))
                .build();
        context.getVariables().forEach(newContext::setVariable);
        return new DefaultLLMSession(null, config, newContext, provider, interceptorChain, persistence);
    }

    @Override
    public void save(String path) {
        if (persistence == null) {
            throw new LLMException("No persistence mechanism configured");
        }
        SessionSnapshot snapshot = SessionSnapshot.from(sessionId, config, context);
        persistence.save(path, snapshot);
        logger.info("Saved session {} to {}", sessionId, path);
    }

    @Override
    public void close() {
        transitionTo(SessionState.CLOSED);
        asyncExecutor.shutdown();
        logger.info("Closed session {}", sessionId);
    }

    private void ensureNotClosed() {
        if (state.get() == SessionState.CLOSED) {
            throw new LLMException("Session is closed");
        }
    }

    private void transitionTo(SessionState newState) {
        state.set(newState);
    }
}
