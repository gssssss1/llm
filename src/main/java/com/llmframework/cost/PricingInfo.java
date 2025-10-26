package com.llmframework.cost;

import com.llmframework.core.usage.Usage;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pricing information for a model
 */
public record PricingInfo(
    BigDecimal inputPricePerMillionTokens,
    BigDecimal outputPricePerMillionTokens,
    String currency
) {
    
    public BigDecimal calculateCost(Usage usage) {
        BigDecimal inputCost = inputPricePerMillionTokens
            .multiply(BigDecimal.valueOf(usage.promptTokens()))
            .divide(BigDecimal.valueOf(1_000_000), 6, RoundingMode.HALF_UP);
        
        BigDecimal outputCost = outputPricePerMillionTokens
            .multiply(BigDecimal.valueOf(usage.completionTokens()))
            .divide(BigDecimal.valueOf(1_000_000), 6, RoundingMode.HALF_UP);
        
        return inputCost.add(outputCost);
    }
    
    public static PricingInfo free() {
        return new PricingInfo(BigDecimal.ZERO, BigDecimal.ZERO, "USD");
    }
    
    public static PricingInfo of(double inputPrice, double outputPrice) {
        return new PricingInfo(
            BigDecimal.valueOf(inputPrice),
            BigDecimal.valueOf(outputPrice),
            "USD"
        );
    }
}
