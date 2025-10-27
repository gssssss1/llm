package com.llm.core.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Message from the user.
 */
public class UserMessage extends AbstractMessage {

    private UserMessage(String id, List<MultiModalContent> contents, Map<String, Object> metadata) {
        super(id, MessageType.USER, contents, metadata);
    }

    public static UserMessage of(String text) {
        return new UserMessage(null, Collections.singletonList(MultiModalContent.text(text)), Collections.emptyMap());
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for user messages.
     */
    public static class Builder extends MessageBuilder<UserMessage, Builder> {

        @Override
        protected UserMessage buildInternal(String id, List<MultiModalContent> contents, Map<String, Object> metadata) {
            return new UserMessage(id, contents, metadata);
        }
    }
}
