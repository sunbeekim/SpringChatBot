package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEEPSEEK_URL = "http://localhost:8000/v1/chat/completions";

    public String chat(String message, List<Map<String, String>> history) {
        System.out.println("=== DeepSeekService 채팅 요청 시작 ===");
        System.out.println("받은 메시지: " + message);
        System.out.println("대화 기록: " + history);
        
        try {
            // DeepSeek API 요청 준비
            URL url = new URL(DEEPSEEK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 메시지 배열 생성
            ArrayNode messages = objectMapper.createArrayNode();
            
            // 대화 기록 추가
            if (history != null) {
                for (Map<String, String> msg : history) {
                    if (msg.containsKey("user")) {
                        ObjectNode userMsg = objectMapper.createObjectNode();
                        userMsg.put("role", "user");
                        userMsg.put("content", msg.get("user"));
                        messages.add(userMsg);
                    }
                    if (msg.containsKey("assistant")) {
                        ObjectNode assistantMsg = objectMapper.createObjectNode();
                        assistantMsg.put("role", "assistant");
                        assistantMsg.put("content", msg.get("assistant"));
                        messages.add(assistantMsg);
                    }
                }
            }
            
            // 현재 메시지 추가
            ObjectNode currentMsg = objectMapper.createObjectNode();
            currentMsg.put("role", "user");
            currentMsg.put("content", message);
            messages.add(currentMsg);

            // 요청 본문 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "deepseek-ai/DeepSeek-R1");
            requestBody.set("messages", messages);

            String jsonInputString = requestBody.toString();
            System.out.println("DeepSeek 서버로 보내는 데이터: " + jsonInputString);

            // 요청 전송
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 응답 읽기
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // JSON 응답 파싱
            JsonNode jsonResponse = objectMapper.readTree(response.toString());
            String assistantResponse = jsonResponse.path("choices")
                                                 .path(0)
                                                 .path("message")
                                                 .path("content")
                                                 .asText();

            System.out.println("DeepSeek 응답: " + assistantResponse);
            System.out.println("=== DeepSeekService 채팅 요청 완료 ===");

            return assistantResponse;

        } catch (Exception e) {
            System.out.println("=== DeepSeekService 에러 발생 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 오류가 발생했습니다: " + e.getMessage();
        }
    }
} 