package com.airesume.analyzer.service;

import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.Resume;
import com.airesume.analyzer.entity.SkillKeyword;
import com.airesume.analyzer.service.ats.AtsAnalysisEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AtsAnalysisEngineTest {

    private AtsAnalysisEngine engine;
    private List<SkillKeyword> seedKeywords;

    @BeforeEach
    void setUp() {
        engine = new AtsAnalysisEngine();
        seedKeywords = Arrays.asList(
                SkillKeyword.builder().keyword("Java").category("Backend").weight(5).build(),
                SkillKeyword.builder().keyword("Spring Boot").category("Backend").weight(5).build(),
                SkillKeyword.builder().keyword("React").category("Frontend").weight(4).build(),
                SkillKeyword.builder().keyword("PostgreSQL").category("Database").weight(4).build(),
                SkillKeyword.builder().keyword("Docker").category("DevOps").weight(3).build()
        );
    }

    @Test
    @DisplayName("Complete resume with metrics, action verbs, contact links score near 100")
    void testExcellentResume() {
        ParsedResume parsed = ParsedResume.builder()
                .fullName("Alex Smith")
                .email("alex@example.com")
                .phone("+1-555-0199")
                .location("San Francisco, CA")
                .linkedinUrl("https://linkedin.com/in/alexsmith")
                .githubUrl("https://github.com/alexsmith")
                .summary("Architected and engineered scalable Java Spring Boot microservices handling 50k+ daily transactions with 99.9% uptime.")
                .skills("Java 21, Spring Boot, React, PostgreSQL, Docker, REST API, Git")
                .experience("Architected microservices handling 50k+ transactions. Reduced query latency by 35%. Spearheaded CI/CD deployment with Docker.")
                .education("B.S. in Computer Science")
                .projects("AI Resume Analyzer: Built full-stack ATS evaluation platform.")
                .certifications("AWS Certified Solutions Architect")
                .rawText("Alex Smith alex@example.com Architected and engineered Java Spring Boot microservices. 50k+ transactions, 35% reduction.")
                .build();

        AtsAnalysisEngine.AtsEngineResult result = engine.analyze(parsed, seedKeywords);

        assertNotNull(result);
        assertTrue(result.getOverallScore() >= 80, "Excellent resume should score >= 80");
        assertEquals(10, result.getContactScore());
        assertNotNull(result.getFactorExplanations());
        assertFalse(result.getFactorExplanations().isEmpty());
    }

    @Test
    @DisplayName("Senior candidate with concise resume scores high due to metrics & action verbs rather than text length")
    void testSeniorConciseResume() {
        ParsedResume parsed = ParsedResume.builder()
                .fullName("Sarah Connor")
                .email("sarah@example.com")
                .phone("+1-555-0200")
                .summary("Principal Engineer with 10+ years experience building distributed Java microservices.")
                .skills("Java, Spring Boot, PostgreSQL, Docker")
                .experience("Spearheaded cloud migration of 15 microservices. Optimized PostgreSQL performance by 40%. Reduced downtime to 0.01%. 2014 - Present.")
                .education("M.S. Computer Engineering")
                .rawText("Sarah Connor Principal Engineer 2014 - Present. Spearheaded cloud migration, 40% performance gain.")
                .build();

        AtsAnalysisEngine.AtsEngineResult result = engine.analyze(parsed, seedKeywords);

        assertNotNull(result);
        assertTrue(result.getOverallScore() >= 70, "Senior concise resume with metrics & action verbs should score >= 70");
    }

    @Test
    @DisplayName("Verbose candidate with weak relevance and keyword repetition is penalized for stuffing")
    void testVerboseFluffResume() {
        ParsedResume parsed = ParsedResume.builder()
                .fullName("Bob Fluff")
                .summary("Hardworking team player seeking entry level job.")
                .rawText("Java Java Java Java Java Java Java Java Java Java Java Java worked on things worked on stuff worked on code worked on tasks.")
                .build();

        AtsAnalysisEngine.AtsEngineResult result = engine.analyze(parsed, seedKeywords);

        assertNotNull(result);
        assertTrue(result.getOverallScore() < 60, "Verbose fluff resume should receive low ATS score");
        assertTrue(result.getDuplicateKeywordCount() > 0, "Should detect repeated keyword stuffing");
    }

    @Test
    @DisplayName("Empty or malformed resume returns baseline 0-10 score without exceptions")
    void testEmptyParsedResume() {
        ParsedResume parsed = ParsedResume.builder().build();

        AtsAnalysisEngine.AtsEngineResult result = engine.analyze(parsed, Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.getOverallScore() <= 15, "Empty resume score should be <= 15");
        assertNotNull(result.getFactorExplanations());
    }
}
