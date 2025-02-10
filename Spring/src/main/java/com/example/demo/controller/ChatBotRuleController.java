package com.example.demo.controller;

import com.example.demo.dto.ChatBotRuleDTO;
import com.example.demo.service.ChatBotRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rules")
public class ChatBotRuleController {

    private final ChatBotRuleService chatBotRuleService;

    // 규칙 목록 조회
    @GetMapping
    public ResponseEntity<List<ChatBotRuleDTO>> getRules(
            @RequestParam Integer roleId,
            @RequestParam String username) {
        List<ChatBotRuleDTO> rules = chatBotRuleService.getRules(roleId, username);
        return ResponseEntity.ok(rules);
    }

    // 규칙 추가
    @PostMapping
    public ResponseEntity<ChatBotRuleDTO> addRule(
            @RequestBody Map<String, Object> ruleData) {
        ChatBotRuleDTO savedRule = chatBotRuleService.addRule(ruleData);
        return ResponseEntity.ok(savedRule);
    }

    // 규칙 수정
    @PutMapping("/{id}")
    public ResponseEntity<ChatBotRuleDTO> updateRule(
            @PathVariable Long id,
            @RequestBody Map<String, Object> ruleData) {
        ChatBotRuleDTO updatedRule = chatBotRuleService.updateRule(id, ruleData);
        return ResponseEntity.ok(updatedRule);
    }

    // 규칙 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            @PathVariable Long id,
            @RequestParam Integer roleId,
            @RequestParam String username) {
        chatBotRuleService.deleteRule(id, roleId, username);
        return ResponseEntity.ok().build();
    }

    // 규칙 적용
    @PutMapping("/{id}/apply")
    public ResponseEntity<ChatBotRuleDTO> applyRule(
            @PathVariable Long id,
            @RequestParam String username) {
        ChatBotRuleDTO appliedRule = chatBotRuleService.applyRule(id, username);
        return ResponseEntity.ok(appliedRule);
    }

    // 적용된 규칙 목록 조회
    @GetMapping("/applied")
    public ResponseEntity<List<ChatBotRuleDTO>> getAppliedRules() {
        List<ChatBotRuleDTO> appliedRules = chatBotRuleService.getAppliedRules();
        return ResponseEntity.ok(appliedRules);
    }

    // 규칙 적용 해제
    @PutMapping("/{id}/unapply")
    public ResponseEntity<?> unapplyRule(
            @PathVariable Long id,
            @RequestParam String username
    ) {
        try {
            ChatBotRuleDTO rule = chatBotRuleService.getRule(id, username);
            if (rule == null || !username.equals(rule.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다.");
            }
            chatBotRuleService.unapplyRule(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("규칙 적용 해제 실패: " + e.getMessage());
        }
    }
}
