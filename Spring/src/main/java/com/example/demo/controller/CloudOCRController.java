package com.example.demo.controller;

import com.example.demo.service.CloudOCRService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ocr")
public class CloudOCRController {

    private final CloudOCRService cloudOCRService;

    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> processImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String result = cloudOCRService.processImage(file);
            return ResponseEntity.ok(Map.of(
                "text", result,
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "text", "이미지 처리 중 오류가 발생했습니다.",
                "status", "error",
                "error", e.getMessage()
            ));
        }
    }
}
