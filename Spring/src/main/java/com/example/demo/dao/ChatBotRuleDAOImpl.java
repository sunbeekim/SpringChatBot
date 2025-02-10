package com.example.demo.dao.impl;

import com.example.demo.dao.ChatBotRuleDAO;
import com.example.demo.mapper.ChatBotRuleMapper;
import com.example.demo.model.ChatBotRule;
import com.example.demo.model.ChatBotRuleCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatBotRuleDAOImpl implements ChatBotRuleDAO {
    private final ChatBotRuleMapper chatBotRuleMapper;

    @Override
    public List<ChatBotRule> getRules(Integer roleId, String username) {
        return chatBotRuleMapper.getRules(roleId, username);
    }

    @Override
    public ChatBotRule getRule(Long id, String username) {
        return chatBotRuleMapper.getRule(id, username);
    }

    @Override
    public Long addRule(ChatBotRule rule) {
        chatBotRuleMapper.addRule(rule);
        return rule.getId();
    }

    @Override
    public void updateRule(ChatBotRule rule) {
        chatBotRuleMapper.updateRule(rule);
    }

    @Override
    public void deleteRule(Long id) {
        chatBotRuleMapper.deleteRule(id);
    }

    @Override
    public void applyRule(Long id) {
        chatBotRuleMapper.applyRule(id);
    }

    @Override
    public List<ChatBotRule> getAppliedRules() {
        return chatBotRuleMapper.getAppliedRules();
    }

    @Override
    public void addRuleConditions(Long ruleId, List<ChatBotRuleCondition> conditions) {
        for (ChatBotRuleCondition condition : conditions) {
            condition.setRuleId(ruleId);
            chatBotRuleMapper.addRuleCondition(condition);
        }
    }

    @Override
    public void deleteRuleConditions(Long ruleId) {
        chatBotRuleMapper.deleteRuleConditions(ruleId);
    }

    @Override
    public void unapplyRule(Long id) {
        chatBotRuleMapper.unapplyRule(id);
    }
}