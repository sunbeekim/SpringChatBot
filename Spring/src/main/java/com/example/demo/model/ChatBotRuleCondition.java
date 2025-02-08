package com.example.demo.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatBotRuleCondition {
    private Long id;
    private Long ruleId;
    private String conditionText;
    private String response;
    private LocalDateTime createdAt;
}