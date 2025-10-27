package com.llm.core.message;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a sequence of messages that form a conversation.
 */
public class MessageChain {

    private final List<Message> messages;

    private MessageChain(List<Message> messages) {
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public static MessageChain empty() {
        return new MessageChain(Collections.emptyList());
    }

    public static MessageChain of(Message... messages) {
        return new MessageChain(Arrays.asList(messages));
    }

    public static MessageChain of(List<Message> messages) {
        return new MessageChain(messages);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public Message get(int index) {
        return messages.get(index);
    }

    public Message getLast() {
        if (messages.isEmpty()) {
            throw new NoSuchElementException("MessageChain is empty");
        }
        return messages.get(messages.size() - 1);
    }

    public MessageChain append(Message message) {
        List<Message> newMessages = new ArrayList<>(messages);
        newMessages.add(message);
        return new MessageChain(newMessages);
    }

    public MessageChain append(MessageChain chain) {
        List<Message> newMessages = new ArrayList<>(messages);
        newMessages.addAll(chain.messages);
        return new MessageChain(newMessages);
    }

    public MessageChain subChain(int fromIndex, int toIndex) {
        return new MessageChain(messages.subList(fromIndex, toIndex));
    }

    public MessageChain filterByType(MessageType type) {
        List<Message> filtered = messages.stream()
                .filter(m -> m.getType() == type)
                .collect(Collectors.toList());
        return new MessageChain(filtered);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for message chains.
     */
    public static class Builder {
        private final List<Message> messages = new ArrayList<>();

        public Builder add(Message message) {
            messages.add(message);
            return this;
        }

        public Builder addAll(List<Message> messages) {
            this.messages.addAll(messages);
            return this;
        }

        public Builder system(String text) {
            messages.add(SystemMessage.of(text));
            return this;
        }

        public Builder user(String text) {
            messages.add(UserMessage.of(text));
            return this;
        }

        public Builder assistant(String text) {
            messages.add(AssistantMessage.of(text));
            return this;
        }

        public MessageChain build() {
            return new MessageChain(messages);
        }
    }
}
