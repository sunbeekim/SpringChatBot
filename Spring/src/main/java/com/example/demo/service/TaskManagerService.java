package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskManagerService {
  private final ConcurrentHashMap<String, Boolean> tasks = new ConcurrentHashMap<>();

  // 작업 등록
  public void registerTask(String taskId) {
    tasks.put(taskId, false); // 작업 등록 (false: 중지되지 않음)
  }

  // 작업 중지 요청
  public boolean cancelTask(String taskId) {
    if (tasks.containsKey(taskId)) {
      tasks.put(taskId, true); // 중지 상태로 변경
      return true;
    }
    return false; // 작업이 없거나 이미 중지됨
  }

  // 작업 중지 여부 확인
  public boolean isTaskCanceled(String taskId) {
    return tasks.getOrDefault(taskId, false); // 중지된 작업인지 확인
  }

  // 작업 제거
  public void unregisterTask(String taskId) {
    tasks.remove(taskId); // 작업 목록에서 제거
  }

  // 모든 작업 상태 반환 (디버깅용)
  public ConcurrentHashMap<String, Boolean> getTaskStatuses() {
    return tasks;
  }
}
