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
        saveMessage(username, roleId, "assistant", response, sessionId);
        
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
        // 메시지와 규칙을 매칭하여 적절한 응답 생성
        for (ChatBotRuleDTO rule : rules) {
            if (matchesRule(message, rule)) {
                return getResponseFromRule(message, rule);
            }
        }
        return "죄송합니다. 적절한 응답을 찾을 수 없습니다.";
    }
    
    private boolean matchesRule(String message, ChatBotRuleDTO rule) {
        // 규칙의 트리거 단어들과 메시지 매칭
        return rule.getTriggerWords().stream()
                  .anyMatch(trigger -> message.toLowerCase().contains(trigger.toLowerCase()));
    }
    
    private String getResponseFromRule(String message, ChatBotRuleDTO rule) {
        if ("simple".equals(rule.getType())) {
            return rule.getResponse();
        } else if ("conditional".equals(rule.getType())) {
            // 조건부 응답 처리
            return rule.getConditions().stream()
                      .filter(condition -> message.toLowerCase().contains(condition.getConditionText().toLowerCase()))
                      .findFirst()
                      .map(ChatBotRuleConditionDTO::getResponse)
                      .orElse(rule.getResponse());
        }
        return rule.getResponse();
    }
} 