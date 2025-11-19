package com.sparta.demo4.controller.dto;

public record TokenUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
}
