package com.example.demo.mapper;

import com.example.demo.model.ChatBotRule;
import com.example.demo.model.ChatBotRuleCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatBotRuleMapper {
    List<ChatBotRule> getRules(@Param("roleId") Integer roleId, @Param("username") String username);
    ChatBotRule getRule(@Param("id") Long id, @Param("username") String username);
    void addRule(ChatBotRule rule);
    void updateRule(ChatBotRule rule);
    void deleteRule(@Param("id") Long id);
    void applyRule(@Param("id") Long id);
    List<ChatBotRule> getAppliedRules();
    void addRuleCondition(ChatBotRuleCondition condition);
    void deleteRuleConditions(@Param("ruleId") Long ruleId);
    void unapplyRule(@Param("id") Long id);
} 