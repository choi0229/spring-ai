package com.sparta.demo4.controller.dto;

public record SearchResult(
        String id,
        String content,
        java.util.Map<String, Object> metadata
) {
}
