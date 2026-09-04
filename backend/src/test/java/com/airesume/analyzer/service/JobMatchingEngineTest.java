package com.airesume.analyzer.service;

import com.airesume.analyzer.entity.JobDescription;
import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.entity.SkillKeyword;
import com.airesume.analyzer.service.job.JobMatchingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobMatchingEngineTest {

    private JobMatchingEngine engine;
    private List<SkillKeyword> seedKeywords;

    @BeforeEach
    void setUp() {
        engine = new JobMatchingEngine();
        seedKeywords = Arrays.asList(
                SkillKeyword.builder().keyword("Java").category("Backend").weight(5).build(),
                SkillKeyword.builder().keyword("Spring Boot").category("Backend").weight(5).build(),
                SkillKeyword.builder().keyword("React").category("Frontend").weight(4).build(),
                SkillKeyword.builder().keyword("PostgreSQL").category("Database").weight(4).build(),
                SkillKeyword.builder().keyword("AWS").category("Cloud").weight(4).build(),
                SkillKeyword.builder().keyword("Docker").category("DevOps").weight(3).build()
        );
    }

    @Test
    @DisplayName("1. Excellent match with required skills, experience, title, and education")
    void testExcellentMatch() {
        ParsedResume resume = ParsedResume.builder()
                .fullName("Alex Smith")
                .summary("Senior Java Backend Lead with 6+ years experience building Spring Boot microservices.")
                .skills("Java 21, Spring Boot, PostgreSQL, AWS, Docker, REST API")
                .experience("Senior Java Engineer 2018 - Present. Architected microservices on AWS with PostgreSQL.")
                .education("B.S. Computer Science")
                .projects("Cloud E-Commerce Backend: Built distributed Java services with Spring Boot and AWS.")
                .rawText("Alex Smith Senior Java Backend Lead 2018 - Present Java Spring Boot PostgreSQL AWS Docker REST API")
                .build();

        JobDescription jd = JobDescription.builder()
                .title("Senior Java Engineer")
                .description("Looking for Senior Java Engineer with 5+ years experience in Spring Boot, PostgreSQL, and AWS.")
                .requiredSkills("Java, Spring Boot, PostgreSQL")
                .preferredSkills("AWS, Docker")
                .minimumExperience(5)
                .educationRequirement("Bachelor's Degree in Computer Science")
                .build();

        JobMatchingEngine.MatchEngineResult result = engine.compare(resume, jd, seedKeywords);

        assertNotNull(result);
        assertTrue(result.getOverallMatchPercentage() >= 85, "Excellent match should score >= 85%");
        assertEquals(100, result.getSkillMatchPercentage());
        assertEquals(100, result.getExperienceMatchPercentage());
        assertNotNull(result.getFactorExplanations());
    }

    @Test
    @DisplayName("2. Weak match due to different technical domain and missing experience")
    void testWeakMatch() {
        ParsedResume resume = ParsedResume.builder()
                .summary("Graphic Designer and Content Creator.")
                .skills("Photoshop, Illustrator, Figma, HTML")
                .experience("Graphic Designer 2022 - 2023")
                .build();

        JobDescription jd = JobDescription.builder()
                .title("Senior Java Architect")
                .requiredSkills("Java, Spring Boot, PostgreSQL, Microservices")
                .minimumExperience(8)
                .build();

        JobMatchingEngine.MatchEngineResult result = engine.compare(resume, jd, seedKeywords);

        assertNotNull(result);
        assertTrue(result.getOverallMatchPercentage() < 40, "Weak match should score < 40%");
        assertFalse(result.getMissingSkills().isEmpty(), "Should report missing mandatory skills");
    }

    @Test
    @DisplayName("3. Missing required skill specifically penalizes mandatory skill component")
    void testMissingRequiredSkill() {
        ParsedResume resume = ParsedResume.builder()
                .skills("React, HTML, CSS, JavaScript")
                .experience("Frontend Developer 2020 - Present")
                .build();

        JobDescription jd = JobDescription.builder()
                .title("Backend Java Developer")
                .requiredSkills("Java")
                .preferredSkills("React")
                .build();

        JobMatchingEngine.MatchEngineResult result = engine.compare(resume, jd, seedKeywords);

        assertNotNull(result);
        assertTrue(result.getMissingSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Java")), "Missing mandatory skill Java must be identified");
        assertTrue(result.getSkillMatchPercentage() < 50, "Missing mandatory skill must heavily lower skill match percentage");
    }

    @Test
    @DisplayName("4. Preferred-only skill missing has less penalty than mandatory missing")
    void testPreferredOnlySkill() {
        ParsedResume resume = ParsedResume.builder()
                .skills("Java, Spring Boot, PostgreSQL")
                .experience("Java Developer 2019 - Present")
                .build();

        JobDescription jd = JobDescription.builder()
                .title("Java Developer")
                .requiredSkills("Java, Spring Boot")
                .preferredSkills("Docker, Kubernetes")
                .minimumExperience(3)
                .build();

        JobMatchingEngine.MatchEngineResult result = engine.compare(resume, jd, seedKeywords);

        assertNotNull(result);
        assertTrue(result.getSkillMatchPercentage() >= 70, "100% mandatory skills matched with preferred missing should still score >= 70%");
    }

    @Test
    @DisplayName("5. Senior candidate with concise resume achieves 100% experience match via dates")
    void testSeniorConciseCandidate() {
        ParsedResume resume = ParsedResume.builder()
                .summary("Principal Engineer")
                .skills("Java, Spring Boot, PostgreSQL, AWS")
                .experience("Principal Engineer at Enterprise Inc. 2014 - Present. Led core cloud backend rewrite.")
                .rawText("Principal Engineer 2014 - Present. Java Spring Boot AWS PostgreSQL")
                .build();

        JobDescription jd = JobDescription.builder()
                .title("Principal Engineer")
                .requiredSkills("Java, Spring Boot")
                .minimumExperience(7)
                .build();

        JobMatchingEngine.MatchEngineResult result = engine.compare(resume, jd, seedKeywords);

        assertNotNull(result);
        assertEquals(100, result.getExperienceMatchPercentage(), "Extracted 2014-Present (~12 years) > 7 required => 100% exp match");
    }

    @Test
    @DisplayName("6. Skill Aliases (JS/JavaScript, Postgres/PostgreSQL, AWS/Amazon Web Services, React/React.js) match correctly")
    void testSkillAliasesMatching() {
        ParsedResume resume = ParsedResume.builder()
                .skills("JS, Postgres, Amazon Web Services, React.js")
                .rawText("Skilled in JS, Postgres db, Amazon Web Services, and React.js development.")
                .build();

        JobDescription jd = JobDescription.builder()
                .title("Full Stack Web Developer")
                .requiredSkills("JavaScript, PostgreSQL, AWS, React")
                .build();

        JobMatchingEngine.MatchEngineResult result = engine.compare(resume, jd, seedKeywords);

        assertNotNull(result);
        assertEquals(100, result.getSkillMatchPercentage(), "Aliases JS, Postgres, AWS, React.js must match canonical JavaScript, PostgreSQL, AWS, React");
        assertTrue(result.getMissingSkills().isEmpty(), "No missing skills when aliases match");
    }

    @Test
    @DisplayName("7. No JD specified returns baseline result without crashing")
    void testNoJdSpecified() {
        ParsedResume resume = ParsedResume.builder().skills("Java, Spring Boot").build();
        JobDescription jd = JobDescription.builder().build();

        JobMatchingEngine.MatchEngineResult result = engine.compare(resume, jd, Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.getOverallMatchPercentage() >= 0 && result.getOverallMatchPercentage() <= 100);
    }

    @Test
    @DisplayName("8. No parsed resume specified returns zero match without crashing")
    void testNoResumeSpecified() {
        JobDescription jd = JobDescription.builder().requiredSkills("Java, Spring Boot").build();

        JobMatchingEngine.MatchEngineResult result = engine.compare(null, jd, Collections.emptyList());

        assertNotNull(result);
        assertEquals(0, result.getSkillMatchPercentage());
        assertEquals(0, result.getOverallMatchPercentage());
    }

    @Test
    @DisplayName("9. Deterministic score reproducibility for identical inputs")
    void testDeterministicReproducibility() {
        ParsedResume resume = ParsedResume.builder().skills("Java, Spring Boot, React").rawText("Java developer 2020-2023").build();
        JobDescription jd = JobDescription.builder().requiredSkills("Java, Spring Boot").minimumExperience(3).build();

        JobMatchingEngine.MatchEngineResult run1 = engine.compare(resume, jd, seedKeywords);
        JobMatchingEngine.MatchEngineResult run2 = engine.compare(resume, jd, seedKeywords);

        assertEquals(run1.getOverallMatchPercentage(), run2.getOverallMatchPercentage());
        assertEquals(run1.getSkillMatchPercentage(), run2.getSkillMatchPercentage());
        assertEquals(run1.getExperienceMatchPercentage(), run2.getExperienceMatchPercentage());
    }
}
