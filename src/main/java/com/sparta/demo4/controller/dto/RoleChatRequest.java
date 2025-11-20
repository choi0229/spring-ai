package com.sparta.demo4.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "역할 기반 채팅 요청")
public record RoleChatRequest(
        @Schema(description = "시스템 프롬프트 (역할)", example = "당신은 친절한 선생님입니다.")
        String systemPrompt,

        @Schema(description = "사용자 메시지", example = "Spring Boot를 설명해주세요.")
        String message
) {}
