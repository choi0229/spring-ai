package com.example.loyalty.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "points")
public class PointPolicyProperties {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal defaultRate = BigDecimal.valueOf(0.01);

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal vipRate = BigDecimal.valueOf(0.015);

    @NotNull
    private Map<String, BigDecimal> categoryRateMap = Collections.emptyMap();

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal minUsageAmount = BigDecimal.valueOf(10000);

    @Min(1)
    private int minUsagePoint = 100;

    @Min(1)
    private int expireDays = 365;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal maxBalance = BigDecimal.valueOf(1_000_000);

    public BigDecimal getDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(BigDecimal defaultRate) {
        this.defaultRate = defaultRate;
    }

    public BigDecimal getVipRate() {
        return vipRate;
    }

    public void setVipRate(BigDecimal vipRate) {
        this.vipRate = vipRate;
    }

    public Map<String, BigDecimal> getCategoryRateMap() {
        return categoryRateMap;
    }

    public void setCategoryRateMap(Map<String, BigDecimal> categoryRateMap) {
        this.categoryRateMap = categoryRateMap;
    }

    public BigDecimal getMinUsageAmount() {
        return minUsageAmount;
    }

    public void setMinUsageAmount(BigDecimal minUsageAmount) {
        this.minUsageAmount = minUsageAmount;
    }

    public int getMinUsagePoint() {
        return minUsagePoint;
    }

    public void setMinUsagePoint(int minUsagePoint) {
        this.minUsagePoint = minUsagePoint;
    }

    public int getExpireDays() {
        return expireDays;
    }

    public void setExpireDays(int expireDays) {
        this.expireDays = expireDays;
    }

    public BigDecimal getMaxBalance() {
        return maxBalance;
    }

    public void setMaxBalance(BigDecimal maxBalance) {
        this.maxBalance = maxBalance;
    }
}
