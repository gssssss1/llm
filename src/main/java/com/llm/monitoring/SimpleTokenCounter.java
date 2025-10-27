package com.llm.monitoring;

import com.llm.core.message.AbstractMessage;
import com.llm.core.message.Message;

import java.util.List;

/**
 * Simple token counter using character-based estimation.
 */
public class SimpleTokenCounter implements TokenCounter {

    private static final int CHARS_PER_TOKEN = 4;

    @Override
    public int countTokens(List<Message> messages) {
        int totalChars = 0;
        for (Message message : messages) {
            if (message instanceof AbstractMessage) {
                totalChars += ((AbstractMessage) message).getTextContent().length();
            }
        }
        return totalChars / CHARS_PER_TOKEN;
    }
}
