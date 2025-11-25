package com.example.loyalty.controller.dto;

import java.util.List;

public record RagResponse(String answer, List<DocumentSource> sources) {
}
