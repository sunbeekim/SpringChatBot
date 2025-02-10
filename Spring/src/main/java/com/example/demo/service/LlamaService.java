package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import lombok.Data;
import lombok.AllArgsConstructor;

@Service
@RequiredArgsConstructor
public class LlamaService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String translate(String text, String sourceLang, String targetLang) {
        System.out.println("=== 번역 시작 ===");
        System.out.println(String.format("%s -> %s 번역", sourceLang, targetLang));
        System.out.println("번역할 텍스트: " + text);
        
        try {
            if (text.length() <= 500) {
                return translateChunk(text, sourceLang, targetLang);
            }

            // 500자 이상인 경우 문장 단위로 분할
            List<String> chunks = new ArrayList<>();
            StringBuilder currentChunk = new StringBuilder();
            
            // 문장 단위로 분할 (마침표, 느낌표, 물음표 기준)
            String[] sentences = text.split("(?<=[.!?])\\s+");
            
            for (String sentence : sentences) {
                if (currentChunk.length() + sentence.length() > 450) { // 여유 있게 450자로 제한
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                }
                currentChunk.append(sentence).append(" ");
            }
            
            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
            }

            // 각 청크 번역 후 결합
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
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String urlStr = String.format(
            "https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s",
            encodedText, sourceLang, targetLang
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
    }

    @Data
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }

    public String chat(String message, List<ChatMessage> history) {
        System.out.println("=== LlamaService 채팅 요청 시작 ===");
        System.out.println("받은 메시지: " + message);
        System.out.println("히스토리 크기: " + history.size());
        
        try {
            // 한글 -> 영어 번역
            String translatedMessage = translate(message, "ko", "en");
            
            // 히스토리도 번역하여 파이썬 서버가 기대하는 형식으로 변환
            List<Map<String, String>> translatedHistory = new ArrayList<>();
            for (int i = 0; i < history.size(); i += 2) {
                Map<String, String> conversation = new HashMap<>();
                // 사용자 메시지
                String userMessage = translate(history.get(i).getContent(), "ko", "en");
                conversation.put("user", userMessage);
                
                // 어시스턴트 응답이 있는 경우
                if (i + 1 < history.size()) {
                    String assistantMessage = translate(history.get(i + 1).getContent(), "ko", "en");
                    conversation.put("assistant", assistantMessage);
                }
                
                translatedHistory.add(conversation);
            }
            
            // LLaMA 서버 요청
            URL url = new URL("http://localhost:8001/chat");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = objectMapper.writeValueAsString(Map.of(
                "message", translatedMessage,
                "history", translatedHistory
            ));
            System.out.println("Python 서버로 보내는 데이터: " + jsonInputString);
            
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(jsonInputString);
            }

            // 에러 응답 처리 추가
            if (conn.getResponseCode() >= 400) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    System.out.println("Python 서버 에러 응답: " + errorResponse.toString());
                    return "{\"response\": \"죄송합니다. 서버 오류가 발생했습니다.\"}";
                }
            }

            // LLaMA 응답 읽기
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // JSON 응답 파싱 - 여기서 에러가 발생하면 원본 응답 반환
            try {
                JsonNode jsonResponse = objectMapper.readTree(response.toString());
                String englishResponse = jsonResponse.get("response").asText();
                System.out.println("영어 응답: " + englishResponse);

                // 영어 -> 한글 번역
                String koreanResponse = translate(englishResponse, "en", "ko");
                System.out.println("한글 번역 응답: " + koreanResponse);

                return objectMapper.writeValueAsString(Map.of("response", koreanResponse));
            } catch (Exception e) {
                System.out.println("JSON 파싱 실패, 원본 응답 반환");
                return response.toString(); // LLaMA 서버의 원본 응답을 그대로 반환
            }
            
        } catch (Exception e) {
            System.out.println("=== LlamaService 에러 발생 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            return "{\"response\": \"죄송합니다. 오류가 발생했습니다: " + e.getMessage() + "\"}";
        }
    }
} 