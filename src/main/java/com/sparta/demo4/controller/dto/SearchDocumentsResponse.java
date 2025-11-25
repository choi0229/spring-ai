package com.sparta.demo4.controller.dto;

import java.util.List;

public record SearchDocumentsResponse(
        String query,
        int resultCount,
        List<DocumentInfo> documents
) {}
