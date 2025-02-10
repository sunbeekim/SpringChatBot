package com.example.demo.controller;

import com.example.demo.service.LlamaService;
import com.example.demo.service.LlamaService.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/llama")
public class LlamaController {

    private final LlamaService llamaService;
    private final Map<String, List<ChatMessage>> chatHistories = new HashMap<>();

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, Object> request) {
        System.out.println("=== LlamaController 채팅 요청 받음 ===");
        System.out.println("요청 데이터: " + request);
        
        String message = (String) request.get("message");
        String sessionId = request.getOrDefault("sessionId", "default").toString();
        
        // 세션별 히스토리 관리
        List<ChatMessage> history = chatHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());
        
        String response = llamaService.chat(message, history);
        
        // 히스토리 업데이트
        ChatMessage userMessage = new ChatMessage("user", message);
        ChatMessage assistantMessage = new ChatMessage("assistant", response);
        history.add(userMessage);
        history.add(assistantMessage);
        
        // 히스토리 크기 제한 (최근 10개 메시지만 유지)
        if (history.size() > 10) {
            history = history.subList(history.size() - 10, history.size());
            chatHistories.put(sessionId, history);
        }
        
        System.out.println("히스토리 크기: " + history.size());
        System.out.println("LlamaService 응답: " + response);
        System.out.println("=== LlamaController 처리 완료 ===");
        
        return ResponseEntity.ok(Map.of("response", response));
    }
} 