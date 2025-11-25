package com.sparta.demo4.controller.dto;

import java.util.Map;

public record DocumentInfo(
        String id,
        String content,
        Map<String, Object> metadata
) {}
