package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEEPSEEK_URL = "http://localhost:8000/chat";
    private final Map<String, List<Map<String, String>>> chatHistories = new HashMap<>();

    private String translate(String text, String sourceLang, String targetLang) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String apiKey = "5431a4570f453332ff03";
            String email = "rlatjsql12@gmail.com";
            
            String urlStr = String.format(
                "https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s&key=%s&de=%s",
                encodedText, sourceLang, targetLang, apiKey, email
            );
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            JsonNode jsonResponse = objectMapper.readTree(response.toString());
            return jsonResponse.get("responseData").get("translatedText").asText();
            
        } catch (Exception e) {
            System.out.println("번역 에러: " + e.getMessage());
            return text;  // 번역 실패시 원본 텍스트 반환
        }
    }

    public String chat(String message, String sessionId) {
        System.out.println("=== DeepSeekService 채팅 요청 시작 ===");
        System.out.println("받은 메시지: " + message);
        
        try {
            // 세션별 히스토리 관리
            List<Map<String, String>> history = chatHistories.computeIfAbsent(sessionId, k -> new ArrayList<>());
            
            // 한글 -> 영어 번역
            String translatedMessage = translate(message, "ko", "en");
            System.out.println("영어로 번역된 메시지: " + translatedMessage);

            URL url = new URL(DEEPSEEK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", translatedMessage);
            requestBody.put("history", history);

            String jsonInputString = objectMapper.writeValueAsString(requestBody);
            System.out.println("Python 서버로 보내는 데이터: " + jsonInputString);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            Map<String, Object> responseMap = objectMapper.readValue(response.toString(), Map.class);
            String englishResponse = (String) responseMap.get("response");
            System.out.println("영어 응답: " + englishResponse);

            // 영어 -> 한글 번역
            String koreanResponse = translate(englishResponse, "en", "ko");
            System.out.println("한글 번역 응답: " + koreanResponse);

            // 히스토리 업데이트
            Map<String, String> conversation = new HashMap<>();
            conversation.put("user", translatedMessage);
            conversation.put("assistant", englishResponse);
            history.add(conversation);

            // 히스토리 크기 제한 (최근 5개 대화만 유지)
            if (history.size() > 5) {
                history = history.subList(history.size() - 5, history.size());
                chatHistories.put(sessionId, history);
            }

            return koreanResponse;

        } catch (Exception e) {
            System.out.println("=== DeepSeekService 에러 발생 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 오류가 발생했습니다: " + e.getMessage();
        }
    }
}