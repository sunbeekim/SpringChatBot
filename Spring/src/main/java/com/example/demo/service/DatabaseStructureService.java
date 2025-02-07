package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import java.io.FileInputStream;

@Service
@RequiredArgsConstructor
public class DatabaseStructureService {

    private final SqlSessionFactory sqlSessionFactory;
    private final SqlSession sqlSession;
    private final ResourceLoader resourceLoader;

    /**
     * 크롤링 데이터를 기반으로 동적 테이블 및 XML을 생성하고 데이터를 저장
     */
    public String generateDatabaseStructure(List<Map<String, Object>> results, Map<String, Object> state) {
        String username = ((Map<String, Object>) state.get("user")).get("username").toString();
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String tablePrefix = sanitizeTableName(username + "_" + uniqueId);

        try {
            // 1. 크롤링 결과 테이블 생성
            String createTableSQL = createCrawlTableScript(tablePrefix);
            executeSQL(createTableSQL);

            // 2. 사용자 조건 테이블 생성 (없는 경우)
            createUserConditionTable();

            // 3. 동적 MyBatis XML 생성 - 순서 변경!
            createSpringFiles(tablePrefix, username, uniqueId);
            
            // 잠시 대기하여 파일 생성 완료 확인
            Thread.sleep(1000);

            // 4. 크롤링 데이터 저장
            storeCrawlData(results, tablePrefix);

            // 5. 사용자 조건 테이블에 메타데이터 저장
            storeUserCondition(username, tablePrefix, results.get(0).get("word").toString());

            return tablePrefix;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("데이터베이스 구조 생성 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * MySQL 테이블 이름 규칙에 맞게 변환
     */
    private String sanitizeTableName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
    }

    /**
     * 동적 테이블 생성 SQL 스크립트 작성
     */
    private String createTableScript(String[] fields, String tablePrefix) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tablePrefix + "_crawl_results (");
        sql.append("id INT AUTO_INCREMENT PRIMARY KEY, ");
        for (String field : fields) {
            sql.append(field).append(" VARCHAR(255), ");
        }
        sql.append("question TEXT, answer TEXT, "); // QA 데이터 필드 추가
        sql.append("createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP);");
        return sql.toString();
    }

    /**
     * SQL 실행 (테이블 생성)
     */
    private void executeSQL(String sql) {
        try {
            sqlSession.update("DynamicMapper.executeSQL", sql);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("SQL 실행 중 오류 발생");
        }
    }

    /**
     * 동적 MyBatis XML 및 Mapper 파일 생성
     */
    private void createSpringFiles(String tablePrefix, String username, String uniqueId) {
        try {
            // 매퍼 파일 생성
            String resourcePath = "src/main/resources/mapper";
            Path mapperDir = Paths.get(resourcePath, username);
            Files.createDirectories(mapperDir);

            String xmlContent = generateMyBatisXML(tablePrefix);
            Path xmlPath = mapperDir.resolve(tablePrefix + "_mapper.xml");
            Files.write(xmlPath, xmlContent.getBytes());

            // 새로 생성된 매퍼 로드
            reloadMapper(xmlPath.toAbsolutePath().toString());
            
            System.out.println("매퍼 파일 생성 완료: " + xmlPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("MyBatis XML 생성 중 오류 발생: " + e.getMessage());
        }
    }

    private String generateMyBatisXML(String tablePrefix) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
        
        // namespace를 tablePrefix와 정확히 일치시킴
        xml.append("<mapper namespace=\"").append(tablePrefix).append("\">\n");
        
        // insertData 쿼리
        xml.append("    <insert id=\"insertData\" parameterType=\"map\">\n");
        xml.append("        INSERT INTO ").append(tablePrefix).append("_crawl_results\n");
        xml.append("        (word, content, related_topics)\n");
        xml.append("        VALUES\n");
        xml.append("        (#{word}, #{content}, #{related_topics})\n");
        xml.append("    </insert>\n");
        
        // selectAllData 쿼리 (캐시 조회용)
        xml.append("    <select id=\"selectAllData\" resultType=\"map\">\n");
        xml.append("        SELECT * FROM ").append(tablePrefix).append("_crawl_results\n");
        xml.append("        ORDER BY createdAt DESC\n");
        xml.append("    </select>\n");
        
        xml.append("</mapper>");

        // 디버깅용 로그
        System.out.println("생성된 매퍼 XML:\n" + xml.toString());
        
        return xml.toString();
    }

    private String createCrawlTableScript(String tablePrefix) {
        return "CREATE TABLE " + tablePrefix + "_crawl_results ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "word VARCHAR(255), "
            + "content LONGTEXT, "
            + "related_topics TEXT, "
            + "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
            + ");";
    }

    private void createUserConditionTable() {
        String sql = "CREATE TABLE IF NOT EXISTS user_conditions ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "username VARCHAR(255), "
            + "search_word VARCHAR(255), "
            + "table_name VARCHAR(255), "
            + "mapper_path VARCHAR(255), "
            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "INDEX idx_username (username)"
            + ");";
        executeSQL(sql);
    }
    
    private void storeCrawlData(List<Map<String, Object>> results, String tablePrefix) {
        System.out.println("테이블 프리픽스: " + tablePrefix); // 디버깅용
        
        for (Map<String, Object> result : results) {
            try {
                if (!result.containsKey("fileTree")) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("word", result.get("word"));
                    data.put("content", result.get("content"));
                    
                    Object relatedTopics = result.get("relatedTopics");
                    if (relatedTopics != null) {
                        if (relatedTopics instanceof List) {
                            data.put("related_topics", String.join(", ", (List<String>) relatedTopics));
                        } else {
                            data.put("related_topics", relatedTopics.toString());
                        }
                    }
                    
                    // namespace와 정확히 일치하는지 확인
                    String statementId = tablePrefix + ".insertData";
                    System.out.println("실행할 매퍼 ID: " + statementId); // 디버깅용
                    
                    sqlSession.insert(statementId, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("에러 발생한 데이터: " + result);
                throw new RuntimeException("크롤링 데이터 저장 중 오류 발생: " + e.getMessage());
            }
        }
    }

    private void storeUserCondition(String username, String tablePrefix, String searchWord) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("username", username);
        condition.put("searchWord", searchWord);
        condition.put("tableName", tablePrefix + "_crawl_results");
        condition.put("mapperPath", "mapper/" + username + "/" + tablePrefix + "_mapper.xml");

        try {
            sqlSession.insert("UserConditionMapper.insertCondition", condition);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("사용자 조건 저장 중 오류 발생");
        }
    }

    private void reloadMapper(String xmlPath) throws Exception {
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
            new FileInputStream(xmlPath),
            sqlSessionFactory.getConfiguration(),
            xmlPath,
            sqlSessionFactory.getConfiguration().getSqlFragments()
        );
        xmlMapperBuilder.parse();
        
        System.out.println("매퍼 로드 완료: " + xmlPath);
    }
}
