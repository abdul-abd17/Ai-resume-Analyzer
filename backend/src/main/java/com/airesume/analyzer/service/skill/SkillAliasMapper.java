package com.airesume.analyzer.service.skill;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkillAliasMapper {

    private static final Map<String, String> ALIAS_TO_CANONICAL = new HashMap<>();

    static {
        // JavaScript / JS
        registerAlias("javascript", "javascript", "js", "ecmascript", "es6", "es2015+");

        // React
        registerAlias("react", "react", "react.js", "reactjs", "react native");

        // Node.js
        registerAlias("node.js", "node.js", "node", "nodejs", "node js");

        // PostgreSQL
        registerAlias("postgresql", "postgresql", "postgres", "postgres db", "pg");

        // AWS
        registerAlias("aws", "aws", "amazon web services", "amazon aws");

        // Kubernetes
        registerAlias("kubernetes", "kubernetes", "k8s");

        // Docker
        registerAlias("docker", "docker", "containers", "containerization");

        // TypeScript
        registerAlias("typescript", "typescript", "ts");

        // Spring Boot
        registerAlias("spring boot", "spring boot", "springboot", "spring-boot", "spring framework");

        // Python
        registerAlias("python", "python", "python3", "py");

        // C++
        registerAlias("c++", "c++", "cpp");

        // C#
        registerAlias("c#", "c#", "c-sharp", "c sharp", "cs");

        // Java
        registerAlias("java", "java", "core java", "java 21", "java 17", "java 11", "java 8");

        // Machine Learning / AI
        registerAlias("machine learning", "machine learning", "ml");
        registerAlias("artificial intelligence", "artificial intelligence", "ai");

        // CI/CD
        registerAlias("ci/cd", "ci/cd", "cicd", "continuous integration", "continuous deployment");

        // GCP
        registerAlias("gcp", "gcp", "google cloud", "google cloud platform");

        // Azure
        registerAlias("azure", "azure", "microsoft azure");

        // REST API
        registerAlias("rest api", "rest api", "rest", "restful", "restful api", "restful apis");

        // GraphQL
        registerAlias("graphql", "graphql");

        // SQL & NoSQL
        registerAlias("sql", "sql", "structured query language");
        registerAlias("nosql", "nosql", "non-relational");

        // MongoDB
        registerAlias("mongodb", "mongodb", "mongo");

        // Redis
        registerAlias("redis", "redis");

        // Kafka
        registerAlias("kafka", "kafka", "apache kafka");
    }

    private static void registerAlias(String canonical, String... aliases) {
        for (String alias : aliases) {
            ALIAS_TO_CANONICAL.put(alias.toLowerCase().trim(), canonical.toLowerCase().trim());
        }
    }

    /**
     * Normalizes a raw skill string to its canonical representation if mapped, otherwise trimmed lowercase.
     */
    public static String getCanonicalSkill(String rawSkill) {
        if (rawSkill == null || rawSkill.isBlank()) return "";
        String normalized = rawSkill.trim().toLowerCase();
        return ALIAS_TO_CANONICAL.getOrDefault(normalized, normalized);
    }

    /**
     * Checks if two skill strings represent the same technical skill or alias.
     */
    public static boolean isEquivalent(String skillA, String skillB) {
        if (skillA == null || skillB == null) return false;
        return getCanonicalSkill(skillA).equals(getCanonicalSkill(skillB));
    }

    /**
     * Checks if a target skill (or any of its known aliases) is present in text using exact word boundary matching.
     * Prevents false positive substring matching (e.g. 'Java' matching 'JavaScript', 'C' matching 'CSS').
     */
    public static boolean containsSkill(String text, String skill) {
        if (text == null || text.isBlank() || skill == null || skill.isBlank()) return false;
        String canonical = getCanonicalSkill(skill);

        // Find all variations for this canonical skill
        Set<String> searchTerms = new HashSet<>();
        searchTerms.add(skill.toLowerCase());
        searchTerms.add(canonical);

        ALIAS_TO_CANONICAL.forEach((alias, can) -> {
            if (can.equals(canonical)) {
                searchTerms.add(alias);
            }
        });

        for (String term : searchTerms) {
            Pattern pattern = Pattern.compile("(?i)(?:^|[^\\w+#.-])" + Pattern.quote(term) + "(?:$|[^\\w+#.-])");
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts occurrence frequency of a skill in text using exact word boundary matching.
     */
    public static int countSkillOccurrences(String text, String skill) {
        if (text == null || text.isBlank() || skill == null || skill.isBlank()) return 0;
        String canonical = getCanonicalSkill(skill);

        Set<String> searchTerms = new HashSet<>();
        searchTerms.add(skill.toLowerCase());
        searchTerms.add(canonical);

        ALIAS_TO_CANONICAL.forEach((alias, can) -> {
            if (can.equals(canonical)) {
                searchTerms.add(alias);
            }
        });

        int totalCount = 0;
        for (String term : searchTerms) {
            Pattern pattern = Pattern.compile("(?i)(?:^|[^\\w+#.-])" + Pattern.quote(term) + "(?:$|[^\\w+#.-])");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                totalCount++;
            }
        }
        return totalCount;
    }
}
