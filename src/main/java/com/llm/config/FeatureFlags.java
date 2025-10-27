package com.llm.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Feature flags for toggling optional functionality.
 */
public class FeatureFlags {

    private final Map<String, Boolean> flags;

    private FeatureFlags(Builder builder) {
        this.flags = Collections.unmodifiableMap(new HashMap<>(builder.flags));
    }

    public boolean isEnabled(String feature) {
        return flags.getOrDefault(feature, Boolean.FALSE);
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public static FeatureFlags defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for feature flags.
     */
    public static class Builder {
        private final Map<String, Boolean> flags = new HashMap<>();

        public Builder enable(String feature) {
            flags.put(feature, Boolean.TRUE);
            return this;
        }

        public Builder disable(String feature) {
            flags.put(feature, Boolean.FALSE);
            return this;
        }

        public FeatureFlags build() {
            return new FeatureFlags(this);
        }
    }
}
