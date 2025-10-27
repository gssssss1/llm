package com.llm.persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session persistence.
 */
public class MemoryStore implements SessionPersistence {

    private final Map<String, SessionSnapshot> store = new ConcurrentHashMap<>();

    @Override
    public void save(String path, SessionSnapshot snapshot) {
        store.put(path, snapshot);
    }

    @Override
    public SessionSnapshot load(String path) {
        SessionSnapshot snapshot = store.get(path);
        if (snapshot == null) {
            throw new IllegalArgumentException("No session stored at " + path);
        }
        return snapshot;
    }
}
