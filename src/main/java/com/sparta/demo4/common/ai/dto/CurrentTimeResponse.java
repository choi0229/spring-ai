package com.sparta.demo4.common.ai.dto;

public record CurrentTimeResponse(
        String isoFormat,
        String readableFormat
) {
}
