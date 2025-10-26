package com.llmframework.image;

import com.llmframework.core.model.ReactiveModel;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Image model interface
 */
public interface ImageModel extends ReactiveModel<ImageInput, ImageOutput> {
    
    /**
     * Generate image (convenience method)
     */
    default Mono<List<String>> generate(String prompt) {
        return call(ImageInput.of(prompt))
            .map(ImageOutput::imageUrls);
    }
    
    /**
     * Edit image
     */
    default Mono<List<String>> edit(String originalImage, String prompt) {
        return call(ImageInput.builder()
            .prompt(prompt)
            .image(originalImage)
            .build())
            .map(ImageOutput::imageUrls);
    }
    
    /**
     * Create variation
     */
    default Mono<List<String>> variation(String image, int n) {
        return call(ImageInput.builder()
            .image(image)
            .n(n)
            .build())
            .map(ImageOutput::imageUrls);
    }
}
