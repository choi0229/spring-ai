package com.sparta.demo4.controller.dto;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "코드 생성 요청")
public record CodeRequest(
        @Schema(description = "구현할 기능 설명", example = "1부터 100까지의 합을 계산")
        String description,

        @Schema(description = "프로그래밍 언어", example = "Java")
        String language
) {}