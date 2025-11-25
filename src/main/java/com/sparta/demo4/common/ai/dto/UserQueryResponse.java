package com.sparta.demo4.common.ai.dto;

public record UserQueryResponse(
        String userId,
        String name,
        String email,
        String phone
) {
}
