package com.example.demo.mapper;

import com.example.demo.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void saveMessage(ChatMessage message);
    List<ChatMessage> getMessagesBySessionId(String sessionId);
} 