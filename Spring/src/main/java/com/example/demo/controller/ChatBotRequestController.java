package com.example.demo.controller;

import com.example.demo.service.ChatBotRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
public class ChatBotRequestController {

    private final ChatBotRequestService chatBotRequestService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, Object> request) {
        System.out.println("=== ChatBotRequestController 채팅 요청 받음 ===");
        System.out.println("요청 데이터: " + request);
        
        String message = (String) request.get("message");
        String username = (String) request.getOrDefault("username", "anonymous");
        Integer roleId = (Integer) request.getOrDefault("roleId", 0);
        String sessionId = (String) request.getOrDefault("sessionId", "default");
        
        String response = chatBotRequestService.processChat(message, username, roleId, sessionId);
        
        System.out.println("ChatBotService 응답: " + response);
        System.out.println("=== ChatBotRequestController 처리 완료 ===");
        
        return ResponseEntity.ok(Map.of("response", response));
    }
} 