package com.llm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audit log for recording sensitive operations.
 */
public class AuditLog {

    private static final Logger logger = LoggerFactory.getLogger(AuditLog.class);

    public void record(String event, String details) {
        logger.info("[AUDIT] {} - {}", event, details);
    }
}
