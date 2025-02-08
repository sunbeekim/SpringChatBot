package com.example.demo.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/files")

public class FileController {

  @PostMapping("/download")
  public ResponseEntity<Resource> downloadFile(@RequestBody Map<String, Object> request) {
    String filePath = (String) request.get("filePath");

    try {

      // 파일 경로 설정
      Path resolvedPath = Paths.get("C:\\java\\MainProject\\newtest\\LLaMA\\tempfile", filePath).toAbsolutePath().normalize();
      System.out.println("Resolved Path: " + resolvedPath);
      
      // 디버깅용 경로 출력
      System.out.println("Resolved Path: " + resolvedPath);


      // 리소스 로드
      Resource resource = new UrlResource(resolvedPath.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다: " + resolvedPath);
      }

      // 헤더 설정 및 반환
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
          .body(resource);
    } catch (Exception e) {
      throw new RuntimeException("파일 다운로드 중 오류 발생 - 경로: " + filePath + e);
    }
  }

}
