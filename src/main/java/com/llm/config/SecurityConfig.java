package com.llm.config;

/**
 * Security configuration for content filtering, data masking and audit logging.
 */
public class SecurityConfig {

    private final boolean contentFilterEnabled;
    private final boolean dataMaskingEnabled;
    private final boolean auditLogEnabled;

    private SecurityConfig(Builder builder) {
        this.contentFilterEnabled = builder.contentFilterEnabled;
        this.dataMaskingEnabled = builder.dataMaskingEnabled;
        this.auditLogEnabled = builder.auditLogEnabled;
    }

    public boolean isContentFilterEnabled() {
        return contentFilterEnabled;
    }

    public boolean isDataMaskingEnabled() {
        return dataMaskingEnabled;
    }

    public boolean isAuditLogEnabled() {
        return auditLogEnabled;
    }

    public static SecurityConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for security configuration.
     */
    public static class Builder {
        private boolean contentFilterEnabled = false;
        private boolean dataMaskingEnabled = false;
        private boolean auditLogEnabled = false;

        public Builder contentFilterEnabled(boolean enabled) {
            this.contentFilterEnabled = enabled;
            return this;
        }

        public Builder dataMaskingEnabled(boolean enabled) {
            this.dataMaskingEnabled = enabled;
            return this;
        }

        public Builder auditLogEnabled(boolean enabled) {
            this.auditLogEnabled = enabled;
            return this;
        }

        public SecurityConfig build() {
            return new SecurityConfig(this);
        }
    }
}
