package com.sparta.demo4.controller;

import com.sparta.demo4.controller.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OLLAMA Chat", description = "로컬 AI 모델 채팅 API")
@RestController
@RequestMapping("/api/ollama")
@RequiredArgsConstructor
public class OllamaChatController {
    private final AiChatController chatService;

    @Operation(summary = "기본 채팅", description = "OLLAMA 모델과 기본 대화")
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return response;
    }

//    @Operation(summary = "역할 기반 채팅", description = "System Message로 역할을 부여한 대화")
//    @PostMapping("/chat/role")
//    public ChatResponse chatWithRole(@RequestBody RoleChatRequest request) {
//        String response = chatService.chatWithRole(
//                request.message(),
//                request.systemPrompt()
//        );
//        return new ChatResponse(response);
//    }
//
//    @Operation(summary = "옵션 커스터마이징", description = "Temperature 등 옵션을 동적으로 설정")
//    @PostMapping("/chat/options")
//    public ChatResponse chatWithOptions(@RequestBody OptionsChatRequest request) {
//        String response = chatService.chatWithOptions(
//                request.message(),
//                request.temperature()
//        );
//        return new ChatResponse(response);
//    }
//
//    @Operation(summary = "번역", description = "텍스트를 특정 언어로 번역")
//    @PostMapping("/translate")
//    public ChatResponse translate(@RequestBody TranslateRequest request) {
//        String response = chatService.translateText(
//                request.text(),
//                request.targetLanguage()
//        );
//        return new ChatResponse(response);
//    }
//
//    @Operation(summary = "코드 생성", description = "요구사항에 맞는 코드를 생성")
//    @PostMapping("/code")
//    public ChatResponse generateCode(@RequestBody CodeRequest request) {
//        String response = chatService.generateCode(
//                request.description(),
//                request.language()
//        );
//        return new ChatResponse(response);
//    }
}
