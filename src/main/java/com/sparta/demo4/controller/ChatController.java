package com.sparta.demo4.controller;

import com.sparta.demo4.controller.dto.ChatRequestV2;
import com.sparta.demo4.controller.dto.ChatResponseV2;
import com.sparta.demo4.service.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.naming.LimitExceededException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "+")
public class ChatController {

    private final IChatService chatService;

    /**
     * 기본 채팅 (히스토리 없음)
     * POST /api/chat/simple
     */
    @PostMapping("/simple")
    public ResponseEntity<ChatResponseV2> simpleChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        ChatResponseV2 response = chatService.chat(message);
        return ResponseEntity.ok(response);
    }

    /**
     * 대화 히스토리를 유지하는 채팅
     * POST /api/chat
     */
    @PostMapping
    public ResponseEntity<ChatResponseV2> chat(@RequestBody ChatRequestV2 request) throws LimitExceededException {
        ChatResponseV2 response = chatService.chatWithHistory(request.message(), request.conversationId());
        return ResponseEntity.ok(response);
    }

    /**
     * 스트리밍 채팅
     * POST /api/chat/stream
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        return chatService.chatStream(message);
    }

    /**
     * 대화 히스토리 조회
     * GET /api/chat/history/{conversationId}
     */
    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<Message>> getHistory(@PathVariable String conversationId) {
        List<Message> history = chatService.getConversationHistory(conversationId);
        return ResponseEntity.ok(history);
    }

    /**
     * 대화 삭제
     * DELETE /api/chat/{conversationId}
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        chatService.clearConversationBy(conversationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 모든 대화 삭제
     * DELETE /api/chat
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllConversations() {
        chatService.clearAllConversations();
        return ResponseEntity.noContent().build();
    }

    /**
     * 헬스 체크
     * GET /api/chat/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Spring AI Claude Chat"
        ));
    }

}
