package com.sparta.demo4.controller.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatResponseV2(String message, String conversationId, LocalDateTime timestamp, TokenUsage tokenUsage, String modelName) {
    public static ChatResponseV2 of(String message, String conversationId, LocalDateTime timestamp, TokenUsage tokenUsage, String modelName) {
        return ChatResponseV2.builder()
                .message(message)
                .conversationId(conversationId)
                .timestamp(timestamp)
                .tokenUsage(tokenUsage)
                .modelName(modelName)
                .build();
    }

    public static ChatResponseV2 of(String message, String conversationId, TokenUsage tokenUsage, String modelName){
        return ChatResponseV2.of(message, conversationId, LocalDateTime.now(), tokenUsage, modelName);
    }

    public static ChatResponseV2 of(String message, String conversationId, String modelName){
        return ChatResponseV2.of(message, conversationId, null);
    }
}
