package com.llm.persistence;

/**
 * Persistence layer for storing and restoring session snapshots.
 */
public interface SessionPersistence {

    void save(String path, SessionSnapshot snapshot);

    SessionSnapshot load(String path);
}
