package com.example.loyalty.controller.dto;

import java.util.Map;

public record DocumentInfo(
        String id,
        String content,
        Map<String, Object> metadata
) {}
