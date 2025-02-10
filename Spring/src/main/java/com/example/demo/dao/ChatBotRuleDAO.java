package com.example.demo.dao;

import com.example.demo.model.ChatBotRule;
import com.example.demo.model.ChatBotRuleCondition;
import java.util.List;

public interface ChatBotRuleDAO {
    List<ChatBotRule> getRules(Integer roleId, String username);
    ChatBotRule getRule(Long id, String username);
    Long addRule(ChatBotRule rule);
    void updateRule(ChatBotRule rule);
    void deleteRule(Long id);
    void applyRule(Long id);
    List<ChatBotRule> getAppliedRules();
    void addRuleConditions(Long ruleId, List<ChatBotRuleCondition> conditions);
    void deleteRuleConditions(Long ruleId);
    void unapplyRule(Long id);
} 