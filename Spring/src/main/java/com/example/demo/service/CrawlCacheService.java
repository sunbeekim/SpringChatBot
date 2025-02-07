package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrawlCacheService {
    
    private final SqlSession sqlSession;

    /**
     * 새로운 크롤링 결과를 캐시에 저장
     */
    public void cacheResult(String word, String deep, Map<String, Object> state, String tableName) {
        try {
            String username = ((Map<String, Object>) state.get("user")).get("username").toString();
            
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            params.put("searchWord", word);
            params.put("deep", deep);
            params.put("tableName", tableName);
            params.put("mapperPath", "mapper/" + username + "/" + tableName + "_mapper.xml");

            sqlSession.insert("UserConditionMapper.insertCacheCondition", params);
            

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("크롤링 결과 캐시 저장 중 오류 발생", e);
        }
    }

    /**
     * 캐시된 크롤링 결과가 있는지 확인하고 반환
     */
    public List<Map<String, Object>> getCachedResult(String word, String deep, Map<String, Object> state) {
        try {
            String username = ((Map<String, Object>) state.get("user")).get("username").toString();
            
            // 캐시 조회를 위한 파라미터 설정
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            params.put("searchWord", word);
            params.put("deep", deep);

            // user_conditions 테이블에서 일치하는 조건 검색
            Map<String, Object> condition = sqlSession.selectOne("UserConditionMapper.findMatchingCondition", params);
            
            if (condition != null) {
                // 캐시된 결과가 있으면 해당 테이블에서 데이터 조회
                String tableName = condition.get("table_name").toString();
                return sqlSession.selectList(tableName + ".selectAllData");
            }
            
            return null; // 캐시 미스
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 캐시된 결과가 있는지 확인
     */
    public boolean hasCachedResult(String word, String deep, Map<String, Object> state) {
        try {
            String username = ((Map<String, Object>) state.get("user")).get("username").toString();
            
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            params.put("searchWord", word);
            params.put("deep", deep);

            Integer count = sqlSession.selectOne("UserConditionMapper.countMatchingConditions", params);
            return count != null && count > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}