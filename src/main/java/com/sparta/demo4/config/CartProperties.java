package com.sparta.demo4.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Component
@Validated
@ConfigurationProperties(prefix = "cart")
public class CartProperties {

    @Min(1)
    private int maxItems = 50;

    private Map<String, Double> promotionMultipliers = java.util.Collections.emptyMap();

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }

    public Map<String, Double> getPromotionMultipliers() {
        return promotionMultipliers;
    }

    public void setPromotionMultipliers(Map<String, Double> promotionMultipliers) {
        this.promotionMultipliers = promotionMultipliers;
    }
}
