package com.example.demo.dao;

import com.example.demo.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageDAO {
    void saveMessage(ChatMessage message);
} 