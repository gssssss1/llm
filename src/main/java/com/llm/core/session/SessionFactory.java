package com.llm.core.session;

import com.llm.config.ModelConfig;
import com.llm.core.interceptor.InterceptorChain;
import com.llm.core.interceptor.impl.*;
import com.llm.monitoring.Metrics;
import com.llm.persistence.FileStore;
import com.llm.persistence.SessionPersistence;
import com.llm.persistence.SessionSnapshot;
import com.llm.provider.ProviderAdapter;
import com.llm.provider.ProviderType;
import com.llm.provider.anthropic.AnthropicAdapter;
import com.llm.provider.openai.OpenAIAdapter;
import com.llm.security.BasicContentFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating LLM sessions.
 */
public class SessionFactory {

    private final Map<ProviderType, ProviderAdapter> providerRegistry = new HashMap<>();
    private final SessionPersistence defaultPersistence;
    private final Metrics globalMetrics;

    public SessionFactory() {
        this.defaultPersistence = new FileStore();
        this.globalMetrics = new Metrics();
        registerDefaultProviders();
    }

    public Metrics getGlobalMetrics() {
        return globalMetrics;
    }

    public InterceptorChain buildDefaultInterceptorChain() {
        return InterceptorChain.builder()
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new MetricsInterceptor(globalMetrics))
                .addInterceptor(new RetryInterceptor())
                .addInterceptor(new ErrorHandlerInterceptor())
                .build();
    }

    private void registerDefaultProviders() {
        String openaiApiKey = System.getenv("OPENAI_API_KEY");
        if (openaiApiKey != null) {
            providerRegistry.put(ProviderType.OPENAI, new OpenAIAdapter(openaiApiKey));
        }

        String anthropicApiKey = System.getenv("ANTHROPIC_API_KEY");
        if (anthropicApiKey != null) {
            providerRegistry.put(ProviderType.ANTHROPIC, new AnthropicAdapter(anthropicApiKey));
        }
    }

    /**
     * Registers a provider adapter.
     */
    public void registerProvider(ProviderType type, ProviderAdapter adapter) {
        providerRegistry.put(type, adapter);
    }

    /**
     * Creates a basic session with simple configuration.
     */
    public LLMSession create(ProviderType providerType, String model) {
        ModelConfig modelConfig = ModelConfig.builder()
                .provider(providerType.getId())
                .model(model)
                .build();

        SessionConfig sessionConfig = SessionConfig.builder()
                .modelConfig(modelConfig)
                .build();

        return createSession(sessionConfig);
    }

    /**
     * Creates a session from configuration.
     */
    public LLMSession fromConfig(SessionConfig config) {
        return createSession(config);
    }

    /**
     * Creates a session using builder pattern.
     */
    public Builder builder() {
        return new Builder(this);
    }

    /**
     * Restores a session from saved state.
     */
    public LLMSession restore(String path) {
        SessionSnapshot snapshot = defaultPersistence.load(path);
        SessionContext context = SessionContext.builder()
                .history(new FullMessageHistory())
                .build();
        context.getHistory().addAll(snapshot.toMessages());
        snapshot.getVariables().forEach(context::setVariable);

        return createSession(snapshot.getConfig(), context);
    }

    /**
     * Restores a session from a snapshot object.
     */
    public LLMSession restoreFromSnapshot(SessionSnapshot snapshot) {
        SessionContext context = SessionContext.builder()
                .history(new FullMessageHistory())
                .build();
        context.getHistory().addAll(snapshot.toMessages());
        snapshot.getVariables().forEach(context::setVariable);

        return createSession(snapshot.getConfig(), context);
    }

    private LLMSession createSession(SessionConfig config) {
        return createSession(config, SessionContext.builder().build());
    }

    private LLMSession createSession(SessionConfig config, SessionContext context) {
        ProviderType providerType = ProviderType.fromId(config.getModelConfig().getProvider());
        ProviderAdapter adapter = providerRegistry.get(providerType);
        if (adapter == null) {
            throw new IllegalStateException("No adapter registered for provider: " + providerType);
        }

        InterceptorChain chain = buildDefaultInterceptorChain();

        return new DefaultLLMSession(null, config, context, adapter, chain, defaultPersistence);
    }

    /**
     * Builder for creating sessions with fluent API.
     */
    public static class Builder {
        private final SessionFactory factory;
        private ProviderType providerType;
        private String model;
        private String systemPrompt;
        private ModelConfig modelConfig;
        private SessionConfig sessionConfig;
        private InterceptorChain interceptorChain;

        private Builder(SessionFactory factory) {
            this.factory = factory;
        }

        public Builder provider(ProviderType providerType) {
            this.providerType = providerType;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder modelConfig(ModelConfig modelConfig) {
            this.modelConfig = modelConfig;
            return this;
        }

        public Builder sessionConfig(SessionConfig sessionConfig) {
            this.sessionConfig = sessionConfig;
            return this;
        }

        public Builder interceptorChain(InterceptorChain interceptorChain) {
            this.interceptorChain = interceptorChain;
            return this;
        }

        public LLMSession build() {
            if (sessionConfig == null) {
                if (modelConfig == null) {
                    if (providerType == null || model == null) {
                        throw new IllegalArgumentException("Provider and model are required");
                    }
                    modelConfig = ModelConfig.builder()
                            .provider(providerType.getId())
                            .model(model)
                            .build();
                }
                sessionConfig = SessionConfig.builder()
                        .modelConfig(modelConfig)
                        .build();
            }

            SessionContext.Builder contextBuilder = SessionContext.builder();
            if (systemPrompt != null) {
                contextBuilder.systemPrompt(systemPrompt);
            }
            SessionContext context = contextBuilder.build();

            ProviderType type = ProviderType.fromId(sessionConfig.getModelConfig().getProvider());
            ProviderAdapter adapter = factory.providerRegistry.get(type);
            if (adapter == null) {
                throw new IllegalStateException("No adapter registered for provider: " + type);
            }

            InterceptorChain chain = interceptorChain != null ? interceptorChain : InterceptorChain.builder().build();

            return new DefaultLLMSession(null, sessionConfig, context, adapter, chain, factory.defaultPersistence);
        }
    }
}
