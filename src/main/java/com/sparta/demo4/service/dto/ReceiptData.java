package com.sparta.demo4.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReceiptData(
        @JsonProperty("storeName")
        String storeName,

        @JsonProperty("address")
        String address,

        @JsonProperty("date")
        LocalDate date,

        @JsonProperty("time")
        LocalTime time,

        @JsonProperty("items")
        List<ReceiptItem> items,

        @JsonProperty("subtotal")
        BigDecimal subtotal,

        @JsonProperty("tax")
        BigDecimal tax,

        @JsonProperty("total")
        BigDecimal total,

        @JsonProperty("paymentMethod")
        String paymentMethod
) {
    /**
     * 영수증 항목
     */
    @Builder
    public record ReceiptItem(
            @JsonProperty("name")
            String name,

            @JsonProperty("quantity")
            Integer quantity,

            @JsonProperty("price")
            BigDecimal price,

            @JsonProperty("totalPrice")
            BigDecimal totalPrice
    ){
        // 생성자에서 totalPrice 자동 계산
        public ReceiptItem {
            if(totalPrice == null && quantity != null && price != null) {
                totalPrice = price.multiply(BigDecimal.valueOf(quantity));
            }
        }
    }

    /**
     * 데이터 검증
     */
    public void validate() {
        if(storeName == null || storeName.isBlank()) {
            throw new IllegalArgumentException("가게명은 필수입니다");
        }
        if(total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("총액은 0보다 커야 합니다");
        }
        if(items == null || items.isEmpty()){
            throw new IllegalArgumentException("항목이 최소 1개 이상 필요합니다");
        }
    }

    /**
     * 총액 검증 (subtotal + tax = total)
     */
    public boolean isTotalValid() {
        if(subtotal == null || tax == null || total == null){
            return true; // 값이 없으면 검증 스킵
        }

        BigDecimal calculatedTotal = subtotal.add(tax);
        return calculatedTotal.compareTo(total) == 0;
    }
}
