package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatBotRuleDTO {
    private Long id;
    private String type;
    private List<String> triggerWords;
    private String response;
    private Integer roleId;
    private String username;
    private boolean isApplied;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChatBotRuleConditionDTO> conditions;
}

