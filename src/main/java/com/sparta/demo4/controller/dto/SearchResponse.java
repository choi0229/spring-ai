package com.sparta.demo4.controller.dto;

import java.util.List;

public record SearchResponse(
        String documentId,
        String query,
        int resultCount,
        List<SearchResult> results
) {
}