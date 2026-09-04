package com.airesume.analyzer.config;

import com.airesume.analyzer.entity.SkillKeyword;
import com.airesume.analyzer.repository.SkillKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeywordDataSeeder implements CommandLineRunner {

    private final SkillKeywordRepository skillKeywordRepository;

    @Override
    public void run(String... args) {
        if (skillKeywordRepository.count() > 0) {
            log.info("Skill keywords already seeded in database.");
            return;
        }

        log.info("Seeding initial ATS skill keywords into database...");

        List<SkillKeyword> keywords = Arrays.asList(
                // Programming
                SkillKeyword.builder().category("Programming").keyword("Java").weight(5).isMandatory(true).build(),
                SkillKeyword.builder().category("Programming").keyword("Python").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Programming").keyword("C++").weight(3).isMandatory(false).build(),
                SkillKeyword.builder().category("Programming").keyword("JavaScript").weight(5).isMandatory(true).build(),
                SkillKeyword.builder().category("Programming").keyword("TypeScript").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Programming").keyword("SQL").weight(5).isMandatory(true).build(),
                SkillKeyword.builder().category("Programming").keyword("Go").weight(3).isMandatory(false).build(),
                SkillKeyword.builder().category("Programming").keyword("C#").weight(3).isMandatory(false).build(),

                // Frameworks
                SkillKeyword.builder().category("Frameworks").keyword("Spring Boot").weight(5).isMandatory(true).build(),
                SkillKeyword.builder().category("Frameworks").keyword("Spring Framework").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Frameworks").keyword("React").weight(5).isMandatory(true).build(),
                SkillKeyword.builder().category("Frameworks").keyword("Angular").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Frameworks").keyword("Vue.js").weight(3).isMandatory(false).build(),
                SkillKeyword.builder().category("Frameworks").keyword("Node.js").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Frameworks").keyword("Hibernate").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Frameworks").keyword("Express").weight(3).isMandatory(false).build(),

                // Databases
                SkillKeyword.builder().category("Databases").keyword("PostgreSQL").weight(4).isMandatory(true).build(),
                SkillKeyword.builder().category("Databases").keyword("MySQL").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Databases").keyword("MongoDB").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Databases").keyword("Redis").weight(3).isMandatory(false).build(),
                SkillKeyword.builder().category("Databases").keyword("Oracle").weight(3).isMandatory(false).build(),

                // Cloud & DevOps
                SkillKeyword.builder().category("Cloud").keyword("AWS").weight(5).isMandatory(true).build(),
                SkillKeyword.builder().category("Cloud").keyword("Azure").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Cloud").keyword("Docker").weight(5).isMandatory(true).build(),
                SkillKeyword.builder().category("Cloud").keyword("Kubernetes").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("DevOps").keyword("Git").weight(5).isMandatory(true).build(),
                SkillKeyword.builder().category("DevOps").keyword("CI/CD").weight(4).isMandatory(true).build(),
                SkillKeyword.builder().category("DevOps").keyword("Terraform").weight(3).isMandatory(false).build(),
                SkillKeyword.builder().category("DevOps").keyword("Jenkins").weight(3).isMandatory(false).build(),

                // Testing
                SkillKeyword.builder().category("Testing").keyword("JUnit").weight(4).isMandatory(true).build(),
                SkillKeyword.builder().category("Testing").keyword("Mockito").weight(4).isMandatory(false).build(),
                SkillKeyword.builder().category("Testing").keyword("Selenium").weight(3).isMandatory(false).build(),
                SkillKeyword.builder().category("Testing").keyword("Jest").weight(3).isMandatory(false).build()
        );

        skillKeywordRepository.saveAll(keywords);
        log.info("Successfully seeded {} ATS skill keywords.", keywords.size());
    }
}
