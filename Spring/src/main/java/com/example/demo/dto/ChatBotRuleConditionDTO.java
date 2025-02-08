package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatBotRuleConditionDTO {
    private Long id;
    private Long ruleId;
    private String conditionText;
    private String response;
    private LocalDateTime createdAt;
} 