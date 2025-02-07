// package com.example.demo.service;

// import lombok.RequiredArgsConstructor;
// //import org.springframework.ai.openai.OpenAiChatClient;
// import org.springframework.stereotype.Service;
// import java.util.*;
// import java.util.stream.Collectors;


// //@Service
// //@RequiredArgsConstructor
// public class AIFieldExtractorService {
    

//     //private final OpenAiChatClient openAiChatClient;


//     /**
//      * 크롤링 데이터를 정형화하여 DB에 저장할 형태로 변환
//      */
//     public List<Map<String, Object>> processAndStoreData(List<Map<String, Object>> results) {
//         try {
//             // OpenAI를 사용하여 필드 추출
//             String[] fields = extractFieldsFromData(results);

//             // 데이터를 정형화
//             List<Map<String, Object>> formattedData = formatData(results, fields);

//             // LLaMA 2 7B Chat을 위한 질의응답 데이터셋 생성
//             List<Map<String, String>> qaDataset = generateQADataset(formattedData);

//             // DB 저장 (여기에 DB 저장 로직 추가)
//             saveToDatabase(formattedData, qaDataset);

//             return formattedData;

//         } catch (Exception e) {
//             e.printStackTrace();
//             return Collections.emptyList();
//         }
//     }

//     /**
//      * OpenAI를 사용하여 데이터에서 필드 추출
//      */
//     private String[] extractFieldsFromData(List<Map<String, Object>> results) {
//         try {
//             String prompt = createPrompt(results);
//             String response = openAiChatClient.call(prompt);
//             if (response == null || response.isEmpty()) throw new RuntimeException("Empty response");
//             return parseAIResponse(response);
//         } catch (Exception e) {
//             e.printStackTrace();
//             return new String[]{"title", "description", "url"};
//         }
//     }

//     /**
//      * OpenAI에 보낼 프롬프트 생성
//      */
//     private String createPrompt(List<Map<String, Object>> results) {
//         StringBuilder prompt = new StringBuilder();
//         prompt.append("다음 크롤링 데이터를 분석하여 적절한 데이터베이스 필드명을 추출해주세요.\n");
//         prompt.append("데이터 샘플:\n");

//         if (!results.isEmpty()) {
//             Map<String, Object> sample = results.get(0);
//             prompt.append(sample.toString()).append("\n");
//         }

//         prompt.append("\n필드명 규칙:\n");
//         prompt.append("1. 영문 소문자 사용\n");
//         prompt.append("2. 단어 구분은 언더스코어 사용\n");
//         prompt.append("3. SQL 예약어 피하기\n");
//         prompt.append("4. 최대 길이 30자\n");

//         return prompt.toString();
//     }

//     /**
//      * OpenAI 응답을 파싱하여 필드명 추출
//      */
//     private String[] parseAIResponse(String response) {
//         return Arrays.stream(response.split(","))
//                 .map(String::trim)
//                 .map(field -> field.replaceAll("[^a-z0-9_]", ""))
//                 .filter(field -> !field.isEmpty())
//                 .toArray(String[]::new);
//     }

//     /**
//      * 크롤링 데이터를 변환하여 DB에 저장할 형식으로 정제
//      */
//     private List<Map<String, Object>> formatData(List<Map<String, Object>> results, String[] fields) {
//         List<Map<String, Object>> formattedData = new ArrayList<>();

//         for (Map<String, Object> data : results) {
//             Map<String, Object> formattedEntry = new HashMap<>();

//             for (int i = 0; i < fields.length; i++) {
//                 String field = (i < fields.length) ? fields[i] : "extra_" + i;
//                 Object value = data.values().toArray().length > i ? data.values().toArray()[i] : null;
//                 formattedEntry.put(field, value);
//             }

//             formattedData.add(formattedEntry);
//         }
//         return formattedData;
//     }

//     /**
//      * LLaMA 2 7B Chat을 위한 질의응답 데이터셋 생성
//      */
//     private List<Map<String, String>> generateQADataset(List<Map<String, Object>> formattedData) {
//         List<Map<String, String>> qaDataset = new ArrayList<>();
//         for (Map<String, Object> entry : formattedData) {
//             String prompt = createQAPrompt(entry);
//             String response = openAiChatClient.call(prompt);
//             if (response == null || response.isEmpty()) continue;
//             Map<String, String> qaEntry = parseQAResponse(response);
//             if (!qaEntry.isEmpty()) qaDataset.add(qaEntry);
//         }
//         return qaDataset;
//     }

//     /**
//      * OpenAI를 활용하여 질의응답 데이터 생성 프롬프트
//      */
//     private String createQAPrompt(Map<String, Object> entry) {
//         return "다음 데이터에서 학습 가능한 질의응답을 생성하세요:\n" + entry.toString();
//     }

//     /**
//      * OpenAI 응답에서 QA 데이터 추출
//      */
//     private Map<String, String> parseQAResponse(String response) {
//         String[] lines = response.split("\n");
//         Map<String, String> qaEntry = new HashMap<>();
//         for (String line : lines) {
//             if (line.startsWith("Q:")) {
//                 qaEntry.put("question", line.substring(2).trim());
//             } else if (line.startsWith("A:")) {
//                 qaEntry.put("answer", line.substring(2).trim());
//             }
//         }
//         return qaEntry;
//     }

//     /**
//      * 데이터 및 QA 데이터셋을 DB에 저장
//      */
//     private void saveToDatabase(List<Map<String, Object>> formattedData, List<Map<String, String>> qaDataset) {
//         for (Map<String, Object> entry : formattedData) {
//             try {
//                 sqlSession.insert("DynamicMapper.insertData", entry);
//             } catch (Exception e) {
//                 e.printStackTrace();
//                 throw new RuntimeException("Error inserting formatted data into DB");
//             }
//         }
    
//         for (Map<String, String> qaEntry : qaDataset) {
//             try {
//                 sqlSession.insert("DynamicMapper.insertQAData", qaEntry);
//             } catch (Exception e) {
//                 e.printStackTrace();
//                 throw new RuntimeException("Error inserting QA data into DB");
//             }
//         }
//     }
    
// }
