package com.llm.core.session;

import com.llm.core.message.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Message history that retains all messages.
 */
public class FullMessageHistory implements MessageHistory {

    private final List<Message> messages;

    public FullMessageHistory() {
        this.messages = new ArrayList<>();
    }

    private FullMessageHistory(List<Message> messages) {
        this.messages = new ArrayList<>(messages);
    }

    @Override
    public void add(Message message) {
        messages.add(message);
    }

    @Override
    public void addAll(List<Message> messages) {
        this.messages.addAll(messages);
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
        return new FullMessageHistory(messages);
    }
}
