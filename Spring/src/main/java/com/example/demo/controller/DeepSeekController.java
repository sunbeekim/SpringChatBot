package com.example.demo.controller;

import com.example.demo.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deepseek")
public class DeepSeekController {

    private final DeepSeekService deepSeekService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, Object> request) {
        System.out.println("=== DeepSeekController 채팅 요청 받음 ===");
        System.out.println("요청 데이터: " + request);
        
        String message = (String) request.get("message");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        
        String response = deepSeekService.chat(message, history);
        
        System.out.println("DeepSeekService 응답: " + response);
        System.out.println("=== DeepSeekController 처리 완료 ===");
        
        return ResponseEntity.ok(Map.of("response", response));
    }
} 