package com.llm.core.message;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a multi-modal content element belonging to a message.
 */
public class MultiModalContent {

    /**
     * Supported content types.
     */
    public enum Type {
        TEXT,
        IMAGE,
        FILE
    }

    private final Type type;
    private final String text;
    private final String uri;
    private final byte[] data;
    private final String mimeType;

    private MultiModalContent(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "type");
        this.text = builder.text;
        this.uri = builder.uri;
        this.data = builder.data;
        this.mimeType = builder.mimeType;
    }

    public Type getType() {
        return type;
    }

    public Optional<String> getText() {
        return Optional.ofNullable(text);
    }

    public Optional<String> getUri() {
        return Optional.ofNullable(uri);
    }

    public Optional<byte[]> getData() {
        return Optional.ofNullable(data);
    }

    public Optional<String> getMimeType() {
        return Optional.ofNullable(mimeType);
    }

    /**
     * Creates a text content element.
     *
     * @param text plain text value
     * @return content instance
     */
    public static MultiModalContent text(String text) {
        return builder().type(Type.TEXT).text(text).build();
    }

    /**
     * Creates an image content element from a URI.
     */
    public static MultiModalContent image(String uri) {
        return builder().type(Type.IMAGE).uri(uri).build();
    }

    /**
     * Creates a file content element.
     */
    public static MultiModalContent file(byte[] data, String mimeType) {
        return builder().type(Type.FILE).data(data).mimeType(mimeType).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for multi-modal content.
     */
    public static final class Builder {
        private Type type;
        private String text;
        private String uri;
        private byte[] data;
        private String mimeType;

        private Builder() {
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public MultiModalContent build() {
            return new MultiModalContent(this);
        }
    }
}
