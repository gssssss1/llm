package com.llm.core.session;

import com.llm.core.message.Message;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Maintains a sliding window of the most recent messages.
 */
public class SlidingWindowMessageHistory implements MessageHistory {

    private final int windowSize;
    private final Deque<Message> messages;

    public SlidingWindowMessageHistory(int windowSize) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize must be > 0");
        }
        this.windowSize = windowSize;
        this.messages = new ArrayDeque<>(windowSize);
    }

    private SlidingWindowMessageHistory(int windowSize, List<Message> snapshot) {
        this.windowSize = windowSize;
        this.messages = new ArrayDeque<>(snapshot);
    }

    @Override
    public void add(Message message) {
        if (messages.size() == windowSize) {
            messages.removeFirst();
        }
        messages.addLast(message);
    }

    @Override
    public void addAll(List<Message> messages) {
        messages.forEach(this::add);
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
            messages.removeLast();
        }
    }

    @Override
    public MessageHistory copy() {
        return new SlidingWindowMessageHistory(windowSize, getMessages());
    }
}
