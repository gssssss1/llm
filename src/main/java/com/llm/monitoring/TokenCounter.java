package com.llm.monitoring;

import com.llm.core.message.Message;

import java.util.List;

/**
 * Counts tokens used by messages.
 */
public interface TokenCounter {

    /**
     * Counts the number of tokens in the provided messages.
     */
    int countTokens(List<Message> messages);
}
