package com.sparta.demo4.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "번역 요청")
public record TranslateRequest(
        @Schema(description = "번역할 텍스트", example = "Hello, World!")
        String text,

        @Schema(description = "목표 언어", example = "한국어")
        String targetLanguage
) {}
