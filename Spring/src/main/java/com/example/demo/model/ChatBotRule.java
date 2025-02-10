package com.example.demo.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatBotRule {
    private Long id;
    private String type;
    private String triggerWords;
    private String response;
    private Integer roleId;
    private String username;
    private boolean isApplied;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedAt;
    private List<ChatBotRuleCondition> conditions;
} 