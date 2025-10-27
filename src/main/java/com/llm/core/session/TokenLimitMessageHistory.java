package com.llm.core.session;

import com.llm.core.message.Message;
import com.llm.monitoring.TokenCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * Message history that keeps messages within a token limit.
 */
public class TokenLimitMessageHistory implements MessageHistory {

    private final int maxTokens;
    private final TokenCounter tokenCounter;
    private final List<Message> messages = new ArrayList<>();

    public TokenLimitMessageHistory(int maxTokens, TokenCounter tokenCounter) {
        this.maxTokens = maxTokens;
        this.tokenCounter = tokenCounter;
    }

    private TokenLimitMessageHistory(int maxTokens, TokenCounter tokenCounter, List<Message> snapshot) {
        this.maxTokens = maxTokens;
        this.tokenCounter = tokenCounter;
        this.messages.addAll(snapshot);
    }

    @Override
    public void add(Message message) {
        messages.add(message);
        trim();
    }

    @Override
    public void addAll(List<Message> messages) {
        this.messages.addAll(messages);
        trim();
    }

    @Override
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    @Override
    public void clear() {
        messages.clear();
    }

    @Override
    public void rollback(int steps) {
        if (steps <= 0 || steps > messages.size()) {
            throw new IllegalArgumentException("Invalid rollback steps: " + steps);
        }
        for (int i = 0; i < steps; i++) {
            messages.remove(messages.size() - 1);
        }
    }

    @Override
    public MessageHistory copy() {
        return new TokenLimitMessageHistory(maxTokens, tokenCounter, getMessages());
    }

    private void trim() {
        while (tokenCounter.countTokens(messages) > maxTokens && !messages.isEmpty()) {
            messages.remove(0);
        }
    }
}
