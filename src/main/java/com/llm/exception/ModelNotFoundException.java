package com.llm.exception;

/**
 * Exception thrown when a model is not found or not available.
 */
public class ModelNotFoundException extends ProviderException {

    private final String modelName;

    public ModelNotFoundException(String modelName, String message) {
        super(404, message);
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }
}
