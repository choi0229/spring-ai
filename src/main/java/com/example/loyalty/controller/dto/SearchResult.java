package com.example.loyalty.controller.dto;

public record SearchResult(
        String id,
        String content,
        java.util.Map<String, Object> metadata
) {
}
