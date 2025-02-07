package com.example.demo.controller;

import com.example.demo.service.LlamaService;
import com.example.demo.service.LlamaService.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/llama")
public class LlamaController {

    private final LlamaService llamaService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, Object> request) {
        System.out.println("=== LlamaController 채팅 요청 받음 ===");
        System.out.println("요청 데이터: " + request);
        
        String message = (String) request.get("message");
        List<ChatMessage> history = new ArrayList<>();  // 현재는 빈 히스토리로 시작
        
        String response = llamaService.chat(message, history);
        
        System.out.println("LlamaService 응답: " + response);
        System.out.println("=== LlamaController 처리 완료 ===");
        
        return ResponseEntity.ok(Map.of("response", response));
    }
} 