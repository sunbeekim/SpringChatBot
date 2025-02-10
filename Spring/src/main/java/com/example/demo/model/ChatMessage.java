package com.example.demo.model;

import lombok.Data;

@Data
public class ChatMessage {
    private Long id;
    private String username;
    private Integer roleId;
    private String messageType;
    private String content;
    private String sessionId;
    private String createdAt;
} 