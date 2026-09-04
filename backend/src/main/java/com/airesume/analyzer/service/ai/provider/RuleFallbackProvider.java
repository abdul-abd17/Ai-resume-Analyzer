package com.airesume.analyzer.service.ai.provider;

import org.springframework.stereotype.Component;

@Component
public class RuleFallbackProvider implements AIProvider {

    @Override
    public String generateCompletion(String prompt) {
        return generateCompletionWithSystem("You are an expert HRTech AI resume coach.", prompt);
    }

    @Override
    public String generateCompletionWithSystem(String systemInstruction, String prompt) {
        return "{\n" +
                "  \"professionalSummary\": \"Analysis generated using static rule engine. For AI-driven summary enhancements, configure an API key (OpenAI or Gemini).\",\n" +
                "  \"improvedExperience\": \"• Synthesized rule feedback: Incorporate quantifiable business metrics (e.g., 'Optimized response latency by [VERIFY METRIC]%') and clear impact verbs in each work experience bullet point.\",\n" +
                "  \"improvedProjects\": \"• Rule feedback: Ensure key technologies and project outcomes are clearly highlighted in your project descriptions (e.g., 'Scaled service to handle [VERIFY METRIC] requests/sec').\",\n" +
                "  \"improvedSkills\": \"Extract and organize skills into clear categories (e.g. Core Skills, Frameworks, Tools) for maximum scanner readability.\",\n" +
                "  \"keywordRecommendations\": \"• Ensure target position keywords appear near the top of your resume.\\n• Align skill section terminology with industry standards.\",\n" +
                "  \"atsRecommendations\": \"• Use standard section headers: Work Experience, Education, Skills, Projects.\\n• Avoid complex multi-column layouts or nested tables.\",\n" +
                "  \"grammarRecommendations\": \"• Replace weak action verbs with strong result-oriented verbs.\\n• Prefer active voice sentences over passive voice constructions.\",\n" +
                "  \"interviewPreparation\": \"1. Technical Question: Be prepared to walk through key technical challenges and architectural decisions in your recent projects.\\n2. Behavioral Question: Describe a situation where you had to solve a complex problem under tight deadlines.\",\n" +
                "  \"careerSuggestions\": \"• Tailor your resume keywords to closely match target job descriptions.\\n• Keep certifications and technical achievements updated.\",\n" +
                "  \"overallFeedback\": \"Rule-based preliminary check complete! Configure an AI provider API key to unlock LLM-powered personalized resume rewrites.\"\n" +
                "}";
    }

    @Override
    public String getProviderName() {
        return "RuleFallbackProvider";
    }
}
