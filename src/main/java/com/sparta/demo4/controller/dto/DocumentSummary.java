package com.sparta.demo4.controller.dto;

import java.time.LocalDateTime;

public record DocumentSummary(String id, String filename, String contentType, int chunkCount, LocalDateTime createdAt, int contentLength) {
}
