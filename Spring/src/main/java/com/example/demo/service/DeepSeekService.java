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

    public static class ChatMessage {
        private String user;
        private String assistant;

        public ChatMessage(String user) {
            this.user = user;
        }

        public ChatMessage(String user, String assistant) {
            this.user = user;
            this.assistant = assistant;
        }

        public String getUser() { return user; }
        public String getAssistant() { return assistant; }
        public void setUser(String user) { this.user = user; }
        public void setAssistant(String assistant) { this.assistant = assistant; }
    }

    private String translate(String text, String sourceLang, String targetLang) {
        System.out.println("=== 번역 시작 ===");
        System.out.println(String.format("%s -> %s 번역", sourceLang, targetLang));
        System.out.println("번역할 텍스트: " + text);
        
        try {
            if (text.length() <= 500) {
                return translateChunk(text, sourceLang, targetLang);
            }

            List<String> chunks = new ArrayList<>();
            StringBuilder currentChunk = new StringBuilder();
            String[] sentences = text.split("(?<=[.!?])\\s+");
            
            for (String sentence : sentences) {
                if (currentChunk.length() + sentence.length() > 450) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                }
                currentChunk.append(sentence).append(" ");
            }
            
            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
            }

            StringBuilder translatedText = new StringBuilder();
            for (String chunk : chunks) {
                String translatedChunk = translateChunk(chunk, sourceLang, targetLang);
                translatedText.append(translatedChunk).append(" ");
            }

            return translatedText.toString().trim();

        } catch (Exception e) {
            System.out.println("=== 번역 에러 발생 ===");
            System.out.println("번역 에러: " + e.getMessage());
            e.printStackTrace();
            return text;
        }
    }

    private String translateChunk(String text, String sourceLang, String targetLang) throws Exception {
        System.out.println("=== translateChunk 시작 ===");
        System.out.println("번역할 텍스트: " + text);
        System.out.println("원본 언어: " + sourceLang);
        System.out.println("목표 언어: " + targetLang);
        
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String urlStr = String.format(
            "https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s",
            encodedText, sourceLang, targetLang
        );
        
        System.out.println("요청 URL: " + urlStr);
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        int responseCode = conn.getResponseCode();
        System.out.println("응답 코드: " + responseCode);
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        System.out.println("번역 API 응답: " + response.toString());
        
        JsonNode jsonResponse = objectMapper.readTree(response.toString());
        if (jsonResponse.has("responseData") && 
            jsonResponse.get("responseData").has("translatedText")) {
            String translatedText = jsonResponse.get("responseData").get("translatedText").asText();
            System.out.println("번역 결과: " + translatedText);
            return translatedText;
        } else {
            throw new RuntimeException("번역 실패: " + response.toString());
        }
    }

    public String chat(String message, List<ChatMessage> history) {
        System.out.println("=== DeepSeekService 채팅 요청 시작 ===");
        System.out.println("받은 메시지: " + message);
        
        try {
            // 한글 -> 영어 번역
            String translatedMessage = translate(message, "ko", "en");
            System.out.println("영어로 번역된 메시지: " + translatedMessage);

            URL url = new URL(DEEPSEEK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 히스토리 변환
            List<Map<String, String>> convertedHistory = new ArrayList<>();
            for (ChatMessage msg : history) {
                if (msg.getUser() != null) {
                    Map<String, String> userMsg = new HashMap<>();
                    userMsg.put("user", msg.getUser());
                    convertedHistory.add(userMsg);
                }
                if (msg.getAssistant() != null) {
                    Map<String, String> assistantMsg = new HashMap<>();
                    assistantMsg.put("assistant", msg.getAssistant());
                    convertedHistory.add(assistantMsg);
                }
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", translatedMessage);
            requestBody.put("history", convertedHistory);

            String jsonInputString = objectMapper.writeValueAsString(requestBody);
            System.out.println("Python 서버로 보내는 데이터: " + jsonInputString);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            Map<String, Object> responseMap = objectMapper.readValue(response.toString(), Map.class);
            
            if ("success".equals(responseMap.get("status"))) {
                String englishResponse = (String) responseMap.get("response");
                System.out.println("영어 응답: " + englishResponse);

                // 영어 -> 한글 번역
                String koreanResponse = translate(englishResponse, "en", "ko");
                System.out.println("한글 번역 응답: " + koreanResponse);

                return koreanResponse;
            } else {
                throw new RuntimeException("응답 실패");
            }

        } catch (Exception e) {
            System.out.println("=== DeepSeekService 에러 발생 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 오류가 발생했습니다: " + e.getMessage();
        }
    }
}