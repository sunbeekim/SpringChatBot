package com.example.demo.service;

import com.example.demo.util.PythonScriptExecutor;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.apache.ibatis.session.SqlSession;

import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CrawlService {

  private final SqlSession sqlSession;
  private final PythonScriptExecutor scriptExecutor;
  private final TaskManagerService taskManagerService;
  private final FileService fileService;
  private final DatabaseStructureService databaseStructureService;
  private final CrawlCacheService crawlCacheService;
  private int maxDepth = 0; // 최대 반복 횟수

  private static final ThreadLocal<Set<String>> processedWords = ThreadLocal.withInitial(HashSet::new); // 스레드별로 처리된 단어
                                                                                                        // 관리

  private static int successCount = 0; // 성공한 작업 수
  private static int failureCount = 0; // 실패한 작업 수

  @Transactional
  public List<Map<String, Object>> processWordWithUrlAndState(String word, String url, String deep,
      Map<String, Object> state, String taskId) {
    // 캐시된 결과가 있는지 확인
    List<Map<String, Object>> cachedResult = crawlCacheService.getCachedResult(word, deep, state);
    if (cachedResult != null) {
      System.out.println("캐시된 결과를 반환합니다: " + word);
      
      // 사용자 디렉토리 설정
      Map<String, Object> user = (Map<String, Object>) state.get("user");
      String username = user.get("username").toString();
      Path initialDirectory = Paths.get("C:\\java\\MainProject\\newtest\\LLaMA\\tempfile", username);
      
      // 파일 트리 생성
      Map<String, Object> fileTree = fileService.getFileTree(initialDirectory.toString());
      
      // 파일 트리를 리스트에 추가
      Map<String, Object> fileTreeMap = new HashMap<>();
      fileTreeMap.put("fileTree", fileTree);
      
      // 캐시된 결과에 파일트리 추가
      cachedResult.add(fileTreeMap);
      
      cachedResult.add(Map.of("summary", "이전 검색결과가 있습니다."));

      return cachedResult;

    }

    // 캐시된 결과가 없으면 기존 로직 실행
    try {
      this.maxDepth = Integer.parseInt(deep);

      // 사용자 디렉토리를 초기 디렉토리로 설정
      Map<String, Object> user = (Map<String, Object>) state.get("user");
      String username = user.get("username").toString();
      
      // 사용자별 디렉토리 경로 설정
      Path initialDirectory = Paths.get("C:\\java\\MainProject\\newtest\\LLaMA\\tempfile", username);
      
      // 디렉토리가 없으면 생성
      try {
          if (!Files.exists(initialDirectory)) {
              Files.createDirectories(initialDirectory);
              System.out.println("사용자 디렉토리 생성 완료: " + initialDirectory);
          }
      } catch (IOException e) {
          System.err.println("디렉토리 생성 실패: " + e.getMessage());
          throw new RuntimeException("사용자 디렉토리 생성 중 오류 발생", e);
      }

      // 초기 입력값만 별도로 추가
      Set<String> words = processedWords.get();
      words.add(word); // 초기 단어 추가
      System.out.println("[초기 단어 추가] processedWords 상태: " + words);

      // 재귀 호출 메서드 시작 (depth를 0으로 시작)
      List<Map<String, Object>> result = processWordRecursive(word, url, 0, state, taskId, initialDirectory);

      // 파일 트리 생성
      Map<String, Object> fileTree = fileService.getFileTree(initialDirectory.toString());

      // 파일 트리를 리스트에 추가하기 위해 Map으로 변환
      Map<String, Object> fileTreeMap = new HashMap<>();
      fileTreeMap.put("fileTree", fileTree);

      // 결과 병합
      List<Map<String, Object>> results = new ArrayList<>(result); // 기존 결과 추가
      results.add(fileTreeMap); // 파일 트리 추가

      // 데이터베이스 구조 생성 및 결과 저장
      
      String tablePrefix = databaseStructureService.generateDatabaseStructure(results, state);
      
      // 새로운 결과를 캐시에 저장
      crawlCacheService.cacheResult(word, deep, state, tablePrefix);

      results.add(Map.of("summary", printSummary()));

      return results;

    } finally {
      processedWords.remove(); // 상태 초기화
    }
  }

  private List<Map<String, Object>> processWordRecursive(String word, String url, int depth,
      Map<String, Object> state, String taskId, Path currentDirectory) {
    List<Map<String, Object>> results = new ArrayList<>();

    // 작업 중지 여부 확인
    if (taskManagerService.isTaskCanceled(taskId)) {
      System.out.println("작업이 중지되었습니다 (재귀 중): " + taskId);
      return results;
    }

    try {
      // 깊이 제한 확인
      if (depth >= maxDepth) {
        System.out.println("최대 재귀 깊이에 도달: " + word);
        return results;
      }

      System.out.println("현재 재귀 깊이: " + depth + " (단어: " + word + ")");

      // 디렉토리 설정
      Path nextDirectory = (depth == maxDepth - 1)
          ? currentDirectory.resolve("finishDeep")
          : currentDirectory.resolve(word);

      Files.createDirectories(nextDirectory);

      // Python 스크립트 실행
      String scriptOutput = scriptExecutor.executeScript(url, word, nextDirectory);
      System.out.println("Python script output:\n" + scriptOutput);

      Path filePath = nextDirectory.resolve(word + ".txt");

      // 결과 파일 확인 및 처리
      List<String> lines = Files.exists(filePath) ? Files.readAllLines(filePath, StandardCharsets.UTF_8) : List.of();

      // "같이 보기" 섹션 처리
      Set<String> relatedTopicsSet = new HashSet<>();
      boolean isInRelatedSection = false;
      for (String line : lines) {
        line = line.trim();
        if (line.equals("같이 보기")) {
          isInRelatedSection = true;
          continue;
        }
        if (isInRelatedSection && !line.isEmpty()) {
          String[] topics = line.split(",");
          for (String topic : topics) {
            relatedTopicsSet.add(topic.trim());
          }
        } else {
          break;
        }
      }
      List<String> relatedTopics = new ArrayList<>(relatedTopicsSet);

      // 처리된 데이터 저장
      Map<String, Object> response = new HashMap<>();
      response.put("word", word);
      response.put("relatedTopics", relatedTopics);
      response.put("content", String.join("\n", lines));
      results.add(response);

      successCount++;
      System.out.println("[재귀 호출 시작] 현재 단어: " + word + " | 관련 토픽: " + relatedTopics);

      // 재귀 호출
      for (String topic : relatedTopics) {
        if (!isAlreadyProcessed(topic)) {
          System.out.println("재귀 호출 단어: " + topic);
          results.addAll(processWordRecursive(topic, url, depth + 1, state, taskId, nextDirectory));

        }
      }

    } catch (Exception e) {
      System.err.println("작업 실패: " + word + " - " + e.getMessage());
      failureCount++;
    }

    return results;
  }

  private boolean isAlreadyProcessed(String word) {
    Set<String> words = processedWords.get();
    if (words.contains(word)) {
      System.out.println("[중복 단어] 이미 처리된 단어: " + word);
      return true;
    }
    words.add(word); // 재귀 호출 중 처리되는 단어만 추가
    System.out.println("[새 단어] 추가된 단어: " + word);
    return false;
  }

  public String printSummary() {
    return "성공한 작업 수: " + successCount + "\n" + "실패한 작업 수: " + failureCount;
  }

  public void resetCounters() {
    successCount = 0;
    failureCount = 0;
  }

}
