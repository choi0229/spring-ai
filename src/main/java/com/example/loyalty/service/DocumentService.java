package com.example.loyalty.service;

import com.example.loyalty.entity.DocumentEntity;
import com.example.loyalty.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final VectorStore vectorStore;

    /**
     * 문서 업로드 및 벡터화
     */
    @Transactional
    public DocumentEntity uploadDocument(MultipartFile file) throws IOException {
        log.info("문서 업로드 시작: {}", file.getOriginalFilename());

        // 1. 파일 내용 읽기
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        // 2. DB에 원본 문서 저장
        DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.randomUUID())
                .filename(file.getOriginalFilename())
                .content(content)
                .contentType(file.getContentType())
                .build();

        // 3. 문서를 작은 청크로 분할
        List<Document> chunks = splitDocument(content, documentEntity.getId().toString(), file.getOriginalFilename());
        documentEntity.setChunkCount(chunks.size());
        documentRepository.save(documentEntity);

        // 4. 벡터 저장소에 저장(자동으로 임베딩 생성)
        vectorStore.add(chunks);

        log.info("문서 업로드 완료: {} ({} chunks})", file.getOriginalFilename(), chunks.size());
        return documentEntity;
    }

    /**
     * 문서 분할 (Chunking)
     */
    private List<Document> splitDocument(String content, String documentId, String filename) {
        // TokenTextSplitter: 토큰 기반으로 문서 분할
        TextSplitter splitter = new TokenTextSplitter(500, 100, 5, 1, true);

        // 메타데이터 추가
        Map<String,Object> metadata = new HashMap<>();
        metadata.put("document_id", documentId);
        metadata.put("filename", filename);
        metadata.put("source", "user_upload");

        // Document객체 생성
        Document document = new Document(content,metadata);
        List<Document> originalChunks = splitter.split(document);

        // 각 청크에 고유 ID 부여
        // 코드를 변경하게 된 사유
        // 기존에는 UUID 값을 Entity에서 자동 생성으로 할당하는 방식을 채택 했으나,
        // 해당하는 로직이 삭제 되면, 아래와 같이 Chunk 별로 UUID 값을 랜덤하게 생성해서 할당해줘야하는 코드가 추가적으로 필요함.
        // 그렇지 않으면 충돌 문제가 발생
        return originalChunks.stream()
                .map(chunk -> {
                    Map<String, Object> newMetadata = new HashMap<>(chunk.getMetadata());
                    return new Document(UUID.randomUUID().toString(), chunk.getText(), newMetadata);
                })
                .toList();
    }

    /**
     * TextReader를 사용한 문서 분할(대안)
     */
    private List<Document> splitDocumentWithReader(MultipartFile file, String documentId) throws IOException {
        // ByteArrayResource로 변환
        Resource resource = new ByteArrayResource(file.getBytes()){
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        // TextReader 사용
        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("document_id", documentId);
        textReader.getCustomMetadata().put("filename", file.getOriginalFilename());
        textReader.getCustomMetadata().put("source", "user_upload");

        List<Document> documents = textReader.get();

        // TokenTextSplitter로 분할
        TextSplitter splitter = new TokenTextSplitter();
        return splitter.split(documents);
    }

    /**
     * 텍스트로 직접 문서 추가
     */
    @Transactional
    public DocumentEntity addTextDocument(String filename, String content) {
        log.info("텍스트 문서 추가: {}", filename);

        DocumentEntity documentEntity = DocumentEntity.builder()
                .id(UUID.randomUUID())
                .filename(filename)
                .content(content)
                .contentType("text/plain")
                .build();

        List<Document> chunks = splitDocument(content, documentEntity.getId().toString(), filename);
        documentEntity.setChunkCount(chunks.size());
        documentRepository.save(documentEntity);

        vectorStore.add(chunks);

        return documentEntity;
    }

    /**
     * 모든 문서 조회
     */
    public List<DocumentEntity> getAllDocuments(){
        return documentRepository.findAll();
    }

    /**
     * 문서 삭제
     */
    @Transactional
    public void deleteDocument(UUID documentId) {
        // DB에서 삭제
        documentRepository.findById(documentId.toString())
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다:"+ documentId));

        documentRepository.deleteById(documentId.toString());

        try{
            List<String> documentIds = vectorSearchRequest(documentId.toString(), "", 1000).stream()
                    .map(Document::getId).toList();

            if(!documentIds.isEmpty()){
                vectorStore.delete(documentIds);
                log.info("Vector Store 삭제에서 {} 청크 삭제 완료", documentIds.size());
            }
        }catch(Exception e) {
            log.error("Vector Store 삭제 주 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 특정 문서의 벡터 검색
     */
    public List<Document> searchDocument(String documentId, String query, int topK) {
        return vectorSearchRequest(documentId, query, topK);
    }

    private List<Document> vectorSearchRequest(String documentId, String query, int topK) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .filterExpression("document_id == '" + documentId + "'")
                        .topK(topK)
                        .build()
        );
    }
}
