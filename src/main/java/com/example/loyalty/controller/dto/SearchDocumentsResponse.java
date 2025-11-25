package com.example.loyalty.controller.dto;

import java.util.List;

public record SearchDocumentsResponse(
        String query,
        int resultCount,
        List<DocumentInfo> documents
) {}
