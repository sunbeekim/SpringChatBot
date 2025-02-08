package com.example.demo.dao.impl;

import com.example.demo.dao.ChatBotRuleDAO;
import com.example.demo.model.ChatBotRule;
import com.example.demo.model.ChatBotRuleCondition;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ChatBotRuleDAOImpl implements ChatBotRuleDAO {
    private final SqlSessionTemplate sqlSession;
    private final String NAMESPACE = "ChatBotRuleMapper.";

    @Override
    public List<ChatBotRule> getRules(Integer roleId, String username) {
        return sqlSession.selectList(NAMESPACE + "getRules", Map.of("roleId", roleId, "username", username));
    }

    @Override
    public ChatBotRule getRule(Long id, String username) {
        return sqlSession.selectOne(NAMESPACE + "getRule", Map.of("id", id, "username", username));
    }

    @Override
    public Long addRule(ChatBotRule rule) {
        sqlSession.insert(NAMESPACE + "addRule", rule);
        return rule.getId();
    }

    @Override
    public void updateRule(ChatBotRule rule) {
        sqlSession.update(NAMESPACE + "updateRule", rule);
    }

    @Override
    public void deleteRule(Long id) {
        sqlSession.delete(NAMESPACE + "deleteRule", id);
    }

    @Override
    public void applyRule(Long id) {
        sqlSession.update(NAMESPACE + "applyRule", id);
    }

    @Override
    public List<ChatBotRule> getAppliedRules() {
        return sqlSession.selectList(NAMESPACE + "getAppliedRules");
    }

    @Override
    public void addRuleConditions(Long ruleId, List<ChatBotRuleCondition> conditions) {
    for (ChatBotRuleCondition condition : conditions) {
        condition.setRuleId(ruleId);
            sqlSession.insert(NAMESPACE + "addRuleCondition", condition);
        }
    }

    @Override
    public void deleteRuleConditions(Long ruleId) {
        sqlSession.delete(NAMESPACE + "deleteRuleConditions", ruleId);
    }
}