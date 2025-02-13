package com.example.demo.controller;

import com.example.demo.service.CloudChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cloudchatbot")
public class CloudChatBotController {

    private final CloudChatBotService cloudChatBotService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String response = cloudChatBotService.getResponse(message);
            
            Map<String, String> responseBody = Map.of(
                "response", response,
                "status", "success"
            );
            
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                "response", "죄송합니다. 오류가 발생했습니다.",
                "status", "error",
                "error", e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Cloud ChatBot Service is running");
    }
} 