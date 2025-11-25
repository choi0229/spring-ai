package com.example.loyalty.controller;

import com.example.loyalty.ai.controller.dto.*;
import com.example.loyalty.controller.dto.*;
import com.example.loyalty.entity.DocumentEntity;
import com.example.loyalty.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ai/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 파일 업르도 및 벡터화
     * POST /api/ai/documents/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> uploadDocument(@RequestPart("file") MultipartFile file)throws IOException {
        log.info("문서 업로드 요청: {}", file.getOriginalFilename());

        DocumentEntity document = documentService.uploadDocument(file);
        return ResponseEntity.ok(new DocumentUploadResponse(
                document.getId().toString(),
                document.getFilename(),
                document.getChunkCount(),
                "문서가 성공적으로 업로드 되었습니다."
        ));
    }

    /**
     * 텍스트로 직접 문서 추가
     * POST /api/ai/documents/text
     */
    @PostMapping("/text")
    public ResponseEntity<DocumentUploadResponse> addTextDocument(@RequestBody TextDocumentRequest request){
        try{
            log.info("텍스트 문서 추가 요청 {}", request.filename());

            DocumentEntity document = documentService.addTextDocument(request.filename(), request.content());

            return ResponseEntity.ok(new DocumentUploadResponse(
                    document.getId().toString(),
                    document.getFilename(),
                    document.getChunkCount(),
                    "텍스트 문서가 성공적으로 추가되었습니다."
            ));
        }catch (Exception e){
            log.error("텍스트 문서 추가 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DocumentUploadResponse(null, null, 0, "문서 추가 실패 "+e));
        }
    }

    /**
     * 모든 문서 조회
     * GET /api/ai/documents
     */
    @GetMapping
    public ResponseEntity<List<DocumentSummary>> getAllDocuments(){
        log.info("전체 문서 목록 조회 요청");

        List<DocumentEntity> documents = documentService.getAllDocuments();

        List<DocumentSummary> summaries = documents.stream()
                .map(doc -> new DocumentSummary(
                        doc.getId().toString(),
                        doc.getFilename(),
                        doc.getContentType(),
                        doc.getChunkCount(),
                        doc.getUploadedAt(),
                        doc.getContent().length()
                ))
                .toList();

        return ResponseEntity.ok(summaries);
    }

    /**
     * 특정 문서 조회
     * GET /api/ai/documents/{documentId}
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentEntity> getDocument(@PathVariable String documentId){
        log.info("문서 조회 요청: {}", documentId);

        return documentService.getAllDocuments().stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 문서 삭제
     * DELETE /api/ai/documents/{documentId}
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<DeleteResponse> deleteDocument(@PathVariable String documentId){
        try{
            log.info("문서 삭제 요청: {}", documentId);

            documentService.deleteDocument(UUID.fromString(documentId));

            return ResponseEntity.ok(new DeleteResponse(
                    documentId,"문서가 성공적으로 삭제되었습니다."
            ));
        }catch(IllegalArgumentException e) {
            log.error("문서 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DeleteResponse(documentId, e.getMessage()));
        } catch (Exception e) {
            log.error("문서 삭제 중 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new DeleteResponse(documentId, "문서 삭제 실패: " + e.getMessage()));
        }
    }

    /**
     * 특정 문서 내에서 검색
     * GET /api/ai/documents/{documentId}/search
     */
    @GetMapping("/{documentId}/search")
    public ResponseEntity<SearchResponse> searchInDocument(
            @PathVariable String documentId,
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        try{
            log.info("문서 내 검색 요청: documentId={}, query={}, topK={}", documentId, query, topK);

            List<Document> results = documentService.searchDocument(documentId, query, topK);

            List<SearchResult> searchResults = results.stream()
                    .map(doc -> new SearchResult(
                            doc.getId(),
                            doc.getText(),
                            doc.getMetadata()
                    ))
                    .toList();

            return ResponseEntity.ok(new SearchResponse(
                    documentId,
                    query,
                    searchResults.size(),
                    searchResults
            ));
        }catch(Exception e) {
            log.error("검색 중 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchResponse(documentId, query, 0, List.of()));
        }

    }
}
