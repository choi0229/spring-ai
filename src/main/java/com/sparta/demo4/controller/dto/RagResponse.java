package com.sparta.demo4.controller.dto;

import java.util.List;

public record RagResponse(String answer, List<DocumentSource> sources) {
}
