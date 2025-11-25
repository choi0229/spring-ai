package com.sparta.demo4.controller.dto;

/**
 * 파일 업로드 응답
 */
public record DocumentUploadResponse(String documentId, String filename, int chunkCount, String message) {
}
