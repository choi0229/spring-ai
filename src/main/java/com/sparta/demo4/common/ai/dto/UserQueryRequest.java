package com.sparta.demo4.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record UserQueryRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("조회할 사용자 ID")
        String userId
) {
}
