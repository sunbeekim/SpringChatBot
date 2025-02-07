package com.example.demo.controller;

import com.example.demo.service.CrawlService;
import com.example.demo.service.TaskManagerService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CrawlController {

  private final CrawlService crawlService;
  private final TaskManagerService taskManagerService; // 작업 관리 서비스

  /**
   * POST 요청을 처리하는 엔드포인트입니다.
   *
   * @param request 클라이언트가 보낸 JSON 데이터를 Map 형식으로 매핑합니다.
   * @return 요청에 대한 처리 결과를 Map으로 반환합니다.
   * @throws IOException
   */
  @PostMapping("/crawling")
  public List<Map<String, Object>> crawl(@RequestBody Map<String, Object> request) throws IOException {
    // 클라이언트가 보낸 데이터에서 값을 추출합니다.
    String word = (String) request.get("word");
    String url = (String) request.get("url");
    String deep = (String) request.get("deep");
    String taskId = (String) request.get("taskId"); // 작업 ID
    Map<String, Object> state = (Map<String, Object>) request.get("state");

    System.out.println("전달받은 단어: " + word); // 디버깅용 로그 출력
    System.out.println("전달받은 URL: " + url); // 디버깅용 로그 출력
    System.out.println("전달받은 DEEP: " + deep);
    System.out.println("전달받은 TaskID: " + taskId);
    System.out.println("전달받은 사용자 상태: " + state); // 디버깅용 로그 출력

    // 작업 등록
    taskManagerService.registerTask(taskId);

    try {
      crawlService.resetCounters();
      // CrawlService를 호출하여 처리 결과를 가져옵니다.
      List<Map<String, Object>> response = crawlService.processWordWithUrlAndState(word, url, deep, state, taskId);
     
      //List<Map<String, Object>>
      // 작업 중지 여부 확인
      if (taskManagerService.isTaskCanceled(taskId)) {
        System.out.println("작업이 중지되었습니다@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@: " + taskId);
        return List.of(Map.of("message", "작업이 중지되었습니다", "taskId", taskId));
      }

      return response;

    } finally {
      // 작업 완료 후 제거
      taskManagerService.unregisterTask(taskId);
    }
  }

  /**
   * 작업 중지를 처리하는 엔드포인트입니다.
   *
   * @param requestBody 클라이언트가 중지할 작업의 ID를 JSON 본문으로 전달합니다.
   * @return 작업 중지 상태를 문자열로 반환합니다.
   */
  @PostMapping("/stopTask")
  public String stopTask(@RequestBody Map<String, String> requestBody) {
    String taskId = requestBody.get("taskId"); // JSON 본문에서 taskId 추출
    if (taskId == null || taskId.isEmpty()) {
      throw new IllegalArgumentException("유효하지 않은 작업 ID입니다.");
    }

    boolean canceled = taskManagerService.cancelTask(taskId);
    if (canceled) {
      System.out.println("작업 중지됨: " + taskId);
      return "작업 중지됨: " + taskId;
    } else {
      System.out.println("작업을 찾을 수 없거나 이미 완료됨: " + taskId);
      return "작업을 찾을 수 없거나 이미 완료됨: " + taskId;
    }
  }

}
