package com.airesume.analyzer.service.ai.prompt;

public class PromptTemplates {

    public static final String SYSTEM_INSTRUCTION =
            "You are an elite, evidence-grounded HRTech AI Career Coach and Resume Optimization Specialist.\n" +
            "Your primary directive is to provide factual, evidence-grounded resume enhancements, ATS placement strategies, and interview prep.\n\n" +
            "STRICT ANTI-HALLUCINATION & EVIDENCE-GROUNDING RULES:\n" +
            "1. THE PARSED RESUME IS THE SINGLE SOURCE OF TRUTH FOR ALL CANDIDATE CLAIMS. Do not invent unverified facts.\n" +
            "2. DO NOT FABRICATE OR INVENT ANY: percentages, dollar amounts, revenue numbers, performance metrics, user counts, team sizes, employment dates, employers, job titles, certifications, degrees, technologies, awards, or achievements.\n" +
            "3. METRIC REWRITING & PLACEHOLDERS: If a candidate's source bullet point contains no numerical metrics, rewrite it to highlight impact WITHOUT inventing numbers, OR use explicit placeholders like '[VERIFY METRIC]' or '[VERIFY X%]'. Never insert fake numbers like '30%' or '$100k'.\n" +
            "4. ANALYTICAL METADATA IS NOT CANDIDATE FACTS: ATS, Grammar, and Job Match reports are analytical diagnostic data about target requirements and scanner rules. Missing job skills represent requirements of the target position, NOT skills the candidate currently possesses.\n" +
            "5. TRACEABILITY: Every suggested improvement must be directly traceable to existing candidate resume content or explicitly marked with '[VERIFY VALUE]'.\n" +
            "6. OUTPUT FORMAT: Respond strictly in raw JSON without markdown formatting delimiters or extraneous text.";

    public static final String COMPREHENSIVE_RECOMMENDATION_PROMPT =
            "Analyze the following candidate resume data and evaluation metadata:\n\n" +
            "=== CANDIDATE PARSED RESUME (SINGLE SOURCE OF TRUTH) ===\n" +
            "Name: %s\n" +
            "Summary: %s\n" +
            "Skills: %s\n" +
            "Experience: %s\n" +
            "Education: %s\n" +
            "Projects: %s\n\n" +
            "=== ANALYTICAL METADATA (ATS REPORT) ===\n" +
            "Overall ATS Score: %d/100\n" +
            "Top Skills: %s\n" +
            "Missing Keywords: %s\n" +
            "Overused Words: %s\n\n" +
            "=== ANALYTICAL METADATA (GRAMMAR REPORT) ===\n" +
            "Grammar Score: %d/100\n" +
            "Spelling Errors: %s\n" +
            "Weak Action Verbs: %s\n" +
            "Passive Voice Count: %d\n\n" +
            "=== ANALYTICAL METADATA (JOB MATCH REPORT) ===\n" +
            "Job Title: %s\n" +
            "Match Percentage: %d%%\n" +
            "Matched Skills: %s\n" +
            "Missing Job Skills: %s\n\n" +
            "CRITICAL DIRECTIVE: Adhere strictly to the anti-hallucination rules. Do not invent metrics or technologies not present in the candidate's parsed resume. Use '[VERIFY METRIC]' if suggesting metric placeholders.\n\n" +
            "Generate a valid JSON object with the following 10 exact keys:\n" +
            "{\n" +
            "  \"professionalSummary\": \"...\",\n" +
            "  \"improvedExperience\": \"...\",\n" +
            "  \"improvedProjects\": \"...\",\n" +
            "  \"improvedSkills\": \"...\",\n" +
            "  \"keywordRecommendations\": \"...\",\n" +
            "  \"atsRecommendations\": \"...\",\n" +
            "  \"grammarRecommendations\": \"...\",\n" +
            "  \"interviewPreparation\": \"...\",\n" +
            "  \"careerSuggestions\": \"...\",\n" +
            "  \"overallFeedback\": \"...\"\n" +
            "}";
}

