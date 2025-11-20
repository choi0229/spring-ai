package com.sparta.demo4.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "옵션 설정 채팅 요청")
public record OptionsChatRequest(
        @Schema(description = "사용자 메시지")
        String message,

        @Schema(description = "창의성 (0.0 ~ 1.0)", example = "0.7")
        double temperature
) {}
