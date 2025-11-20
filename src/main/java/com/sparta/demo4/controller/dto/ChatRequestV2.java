package com.sparta.demo4.controller.dto;

public record ChatRequestV2(String message, String conversationId, String modelName) {
    public ChatRequestV2 {
        if (message == null || message.isBlank()){
            throw new IllegalArgumentException("message is null or blank");
        }
    }
}
