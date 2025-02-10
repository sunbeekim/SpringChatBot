package com.example.demo.service;

import com.example.demo.dao.ChatMessageDAO;
import com.example.demo.model.ChatMessage;
import com.example.demo.dto.ChatBotRuleDTO;
import com.example.demo.dto.ChatBotRuleConditionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatBotRequestService {

    private final ChatBotRuleService chatBotRuleService;
    private final ChatMessageDAO chatMessageDAO;

    @Transactional
    public String processChat(String message, String username, Integer roleId, String sessionId) {
        // 사용자 메시지 저장
        saveMessage(username, roleId, "user", message, sessionId);
        
        // 적용된 규칙들 가져오기
        List<ChatBotRuleDTO> rules = chatBotRuleService.getAppliedRules();
        
        // 규칙에 따른 응답 생성
        String response = generateResponse(message, rules);
        
        // 챗봇 응답 저장
        saveMessage("assistant", 0, "assistant", response, sessionId);
        
        return response;
    }
    
    private void saveMessage(String username, Integer roleId, String messageType, 
                           String content, String sessionId) {
        ChatMessage message = new ChatMessage();
        message.setUsername(username);
        message.setRoleId(roleId);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setSessionId(sessionId);
        
        chatMessageDAO.saveMessage(message);
    }
    
    private String generateResponse(String message, List<ChatBotRuleDTO> rules) {
        for (ChatBotRuleDTO rule : rules) {
            // 트리거 단어 체크
            if (rule.getTriggerWords().stream()
                    .anyMatch(trigger -> message.toLowerCase().contains(trigger.toLowerCase()))) {
                
                // 조건부 응답 체크
                if ("conditional".equals(rule.getType()) && rule.getConditions() != null) {
                    for (ChatBotRuleConditionDTO condition : rule.getConditions()) {
                        if (message.toLowerCase().contains(
                                condition.getConditionText().toLowerCase())) {
                            return condition.getResponse();
                        }
                    }
                }
                
                // 기본 응답 반환
                return rule.getResponse();
            }
        }
        
        return "죄송합니다. 해당 요청에 대한 답변을 찾을 수 없습니다.";
    }
} 