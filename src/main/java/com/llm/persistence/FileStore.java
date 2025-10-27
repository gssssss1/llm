package com.llm.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.llm.exception.LLMException;

import java.io.File;
import java.io.IOException;

/**
 * File-based session persistence.
 */
public class FileStore implements SessionPersistence {

    private final ObjectMapper objectMapper;

    public FileStore() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void save(String path, SessionSnapshot snapshot) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, snapshot);
        } catch (IOException e) {
            throw new LLMException("Failed to save session", e);
        }
    }

    @Override
    public SessionSnapshot load(String path) {
        try {
            return objectMapper.readValue(new File(path), SessionSnapshot.class);
        } catch (IOException e) {
            throw new LLMException("Failed to load session", e);
        }
    }
}
