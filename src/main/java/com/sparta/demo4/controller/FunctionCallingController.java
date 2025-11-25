package com.sparta.demo4.controller;

import com.sparta.demo4.service.FunctionCallingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/function")
@RequiredArgsConstructor
public class FunctionCallingController {

    private final FunctionCallingService functionCallingService;

    /**
     * Function Calling 테스트
     * POST /api/function/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = functionCallingService.chat(message);

        return ResponseEntity.ok(Map.of("response", response));
    }
}
