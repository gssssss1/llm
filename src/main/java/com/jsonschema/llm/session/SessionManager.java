package com.jsonschema.llm.session;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SessionManager {
    private final Map<String, Session> sessions;
    private final Duration sessionTimeout;
    
    public SessionManager() {
        this(Duration.ofHours(1));
    }
    
    public SessionManager(Duration sessionTimeout) {
        this.sessions = new ConcurrentHashMap<>();
        this.sessionTimeout = sessionTimeout;
    }
    
    public Session createSession(Session.Builder builder) {
        Session session = builder.build();
        sessions.put(session.getSessionId(), session);
        return session;
    }
    
    public Session getSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null && isExpired(session)) {
            removeSession(sessionId);
            return null;
        }
        return session;
    }
    
    public List<Session> getUserSessions(String userId) {
        return sessions.values().stream()
                .filter(s -> userId.equals(s.getUserId()))
                .filter(s -> !isExpired(s))
                .collect(Collectors.toList());
    }
    
    public List<Session> getAllSessions() {
        return new ArrayList<>(sessions.values());
    }
    
    public void removeSession(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session != null) {
            session.close();
        }
    }
    
    public void clearExpiredSessions() {
        List<String> expiredIds = sessions.values().stream()
                .filter(this::isExpired)
                .map(Session::getSessionId)
                .collect(Collectors.toList());
        
        for (String id : expiredIds) {
            removeSession(id);
        }
    }
    
    public int getActiveSessionCount() {
        return (int) sessions.values().stream()
                .filter(Session::isActive)
                .filter(s -> !isExpired(s))
                .count();
    }
    
    private boolean isExpired(Session session) {
        Instant lastActivity = session.getLastActivityAt();
        return Duration.between(lastActivity, Instant.now()).compareTo(sessionTimeout) > 0;
    }
}
