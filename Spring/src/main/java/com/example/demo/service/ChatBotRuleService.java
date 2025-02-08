package com.example.demo.service;

import com.example.demo.dao.ChatBotRuleDAO;
import com.example.demo.dto.ChatBotRuleDTO;
import com.example.demo.dto.ChatBotRuleConditionDTO;
import com.example.demo.model.ChatBotRule;
import com.example.demo.model.ChatBotRuleCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatBotRuleService {
    
    private final ChatBotRuleDAO chatBotRuleDAO;

    // DTO -> Model 변환
    private ChatBotRule convertToModel(ChatBotRuleDTO dto) {
        ChatBotRule model = new ChatBotRule();
        model.setId(dto.getId());
        model.setType(dto.getType());
        model.setTriggerWords(String.join(",", dto.getTriggerWords()));
        model.setResponse(dto.getResponse());
        model.setRoleId(dto.getRoleId());
        model.setUsername(dto.getUsername());
        model.setApplied(dto.isApplied());
        model.setAppliedAt(dto.getAppliedAt());
        model.setCreatedAt(dto.getCreatedAt());
        model.setUpdatedAt(dto.getUpdatedAt());
        
        if (dto.getConditions() != null) {
            List<ChatBotRuleCondition> conditions = dto.getConditions().stream()
                .map(conditionDTO -> {
                    ChatBotRuleCondition condition = new ChatBotRuleCondition();
                    condition.setId(conditionDTO.getId());
                    condition.setRuleId(conditionDTO.getRuleId());
                    condition.setConditionText(conditionDTO.getConditionText());
                    condition.setResponse(conditionDTO.getResponse());
                    condition.setCreatedAt(conditionDTO.getCreatedAt());
                    return condition;
                })
                .collect(Collectors.toList());
            model.setConditions(conditions);
        }
        
        return model;
    }

    // Model -> DTO 변환
    private ChatBotRuleDTO convertToDTO(ChatBotRule model) {
        ChatBotRuleDTO dto = new ChatBotRuleDTO();
        dto.setId(model.getId());
        dto.setType(model.getType());
        dto.setTriggerWords(List.of(model.getTriggerWords().split(",")));
        dto.setResponse(model.getResponse());
        dto.setRoleId(model.getRoleId());
        dto.setUsername(model.getUsername());
        dto.setApplied(model.isApplied());
        dto.setAppliedAt(model.getAppliedAt());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setUpdatedAt(model.getUpdatedAt());
        
        if (model.getConditions() != null) {
            List<ChatBotRuleConditionDTO> conditions = model.getConditions().stream()
                .map(condition -> {
                    ChatBotRuleConditionDTO conditionDTO = new ChatBotRuleConditionDTO();
                    conditionDTO.setId(condition.getId());
                    conditionDTO.setRuleId(condition.getRuleId());
                    conditionDTO.setConditionText(condition.getConditionText());
                    conditionDTO.setResponse(condition.getResponse());
                    conditionDTO.setCreatedAt(condition.getCreatedAt());
                    return conditionDTO;
                })
                .collect(Collectors.toList());
            dto.setConditions(conditions);
        }
        
        return dto;
    }

    // 규칙 목록 조회
    public List<ChatBotRuleDTO> getRules(Integer roleId, String username) {
        return chatBotRuleDAO.getRules(roleId, username).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // 규칙 상세 조회
    public ChatBotRuleDTO getRule(Long id, String username) {
        ChatBotRule rule = chatBotRuleDAO.getRule(id, username);
        return rule != null ? convertToDTO(rule) : null;
    }

    // 규칙 추가
    @Transactional
    public ChatBotRuleDTO addRule(Map<String, Object> ruleData) {
        System.out.println("=== ChatBotRuleService addRule 시작 ===");
        System.out.println("받은 데이터: " + ruleData);
        
        ChatBotRuleDTO dto = new ChatBotRuleDTO();
        dto.setType((String) ruleData.get("type"));
        dto.setTriggerWords((List<String>) ruleData.get("triggerWords"));
        dto.setResponse((String) ruleData.get("response"));
        dto.setRoleId((Integer) ruleData.get("roleId"));
        dto.setUsername((String) ruleData.get("username"));
        
        System.out.println("변환된 DTO: " + dto);
        
        ChatBotRule model = convertToModel(dto);
        Long ruleId = chatBotRuleDAO.addRule(model);
        
        if ("conditional".equals(dto.getType())) {
            List<Map<String, String>> conditions = (List<Map<String, String>>) ruleData.get("conditions");
            System.out.println("조건부 규칙 조건들: " + conditions);
            
            if (conditions != null && !conditions.isEmpty()) {
                List<ChatBotRuleCondition> conditionModels = conditions.stream()
                    .map(condition -> {
                        ChatBotRuleCondition conditionModel = new ChatBotRuleCondition();
                        conditionModel.setRuleId(ruleId);
                        conditionModel.setConditionText(condition.get("condition"));
                        conditionModel.setResponse(condition.get("response"));
                        return conditionModel;
                    })
                    .collect(Collectors.toList());
                System.out.println("저장할 조건 모델들: " + conditionModels);
                chatBotRuleDAO.addRuleConditions(ruleId, conditionModels);
            }
        }
        
        ChatBotRuleDTO result = getRule(ruleId, dto.getUsername());
        System.out.println("저장된 결과: " + result);
        System.out.println("=== ChatBotRuleService addRule 완료 ===");
        return result;
    }

    // 규칙 수정
    @Transactional
    public ChatBotRuleDTO updateRule(Long id, Map<String, Object> ruleData) {
        System.out.println("=== ChatBotRuleService updateRule 시작 ===");
        System.out.println("규칙 ID: " + id);
        System.out.println("받은 데이터: " + ruleData);
        
        String username = (String) ruleData.get("username");
        ChatBotRuleDTO dto = getRule(id, username);
        if (dto == null) {
            throw new RuntimeException("Rule not found: " + id);
        }
        
        dto.setType((String) ruleData.get("type"));
        dto.setTriggerWords((List<String>) ruleData.get("triggerWords"));
        dto.setResponse((String) ruleData.get("response"));
        
        System.out.println("수정할 DTO: " + dto);
        
        ChatBotRule model = convertToModel(dto);
        chatBotRuleDAO.updateRule(model);
        
        if ("conditional".equals(dto.getType())) {
            chatBotRuleDAO.deleteRuleConditions(id);
            
            List<Map<String, Object>> conditions = (List<Map<String, Object>>) ruleData.get("conditions");
            System.out.println("새로운 조건들: " + conditions);
            
            if (conditions != null && !conditions.isEmpty()) {
                List<ChatBotRuleCondition> conditionModels = conditions.stream()
                    .map(condition -> {
                        ChatBotRuleCondition conditionModel = new ChatBotRuleCondition();
                        conditionModel.setRuleId(id);
                        conditionModel.setConditionText((String) condition.get("conditionText"));
                        conditionModel.setResponse((String) condition.get("response"));
                        return conditionModel;
                    })
                    .collect(Collectors.toList());
                System.out.println("저장할 조건 모델들: " + conditionModels);
                chatBotRuleDAO.addRuleConditions(id, conditionModels);
            }
        }
        
        ChatBotRuleDTO result = getRule(id, username);
        System.out.println("수정된 결과: " + result);
        System.out.println("=== ChatBotRuleService updateRule 완료 ===");
        return result;
    }

    // 규칙 삭제
    @Transactional
    public void deleteRule(Long id, Integer roleId, String username) {
        ChatBotRuleDTO rule = getRule(id, username);
        if (rule == null) {
            throw new RuntimeException("Rule not found: " + id);
        }
        
        if (!roleId.equals(rule.getRoleId()) || !username.equals(rule.getUsername())) {
            throw new RuntimeException("Unauthorized access");
        }
        
        chatBotRuleDAO.deleteRule(id);
    }

    // 규칙 적용
    @Transactional
    public ChatBotRuleDTO applyRule(Long id, String username) {
        ChatBotRuleDTO dto = getRule(id, username);
        if (dto == null) {
            throw new RuntimeException("Rule not found: " + id);
        }
        
        chatBotRuleDAO.applyRule(id);
        return getRule(id, username);
    }

    // 적용된 규칙 목록 조회
    public List<ChatBotRuleDTO> getAppliedRules() {
        return chatBotRuleDAO.getAppliedRules().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private ChatBotRule getRuleEntity(Long id, String username) {
        ChatBotRule rule = chatBotRuleDAO.getRule(id, username);
        if (rule == null) {
            throw new RuntimeException("Rule not found: " + id);
        }
        return rule;
    }
} 