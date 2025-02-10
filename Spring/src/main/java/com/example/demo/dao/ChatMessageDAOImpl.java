package com.example.demo.dao.impl;

import com.example.demo.dao.ChatMessageDAO;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageDAOImpl implements ChatMessageDAO {
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public void saveMessage(ChatMessage message) {
        chatMessageMapper.saveMessage(message);
    }

    @Override
    public List<ChatMessage> getMessagesBySessionId(String sessionId) {
        return chatMessageMapper.getMessagesBySessionId(sessionId);
    }
} 