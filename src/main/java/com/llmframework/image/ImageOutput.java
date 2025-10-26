package com.llmframework.image;

import com.llmframework.core.model.ModelOutput;
import com.llmframework.core.usage.Usage;

import java.time.Duration;
import java.util.List;

/**
 * Image model output
 */
public record ImageOutput(
    String requestId,
    List<String> imageUrls,
    List<String> imageBase64List,
    Usage usage,
    String modelVersion,
    Duration duration,
    boolean fromCache
) implements ModelOutput {
    
    public String getFirstUrl() {
        return imageUrls.isEmpty() ? null : imageUrls.get(0);
    }
    
    public String getFirstBase64() {
        return imageBase64List.isEmpty() ? null : imageBase64List.get(0);
    }
}
