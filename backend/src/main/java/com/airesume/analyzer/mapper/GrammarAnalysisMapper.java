package com.airesume.analyzer.mapper;

import com.airesume.analyzer.dto.GrammarAnalysisDto;
import com.airesume.analyzer.entity.GrammarAnalysis;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GrammarAnalysisMapper {

    private final ObjectMapper objectMapper;

    public GrammarAnalysisDto toDto(GrammarAnalysis entity) {
        if (entity == null) return null;

        int gramScore = entity.getGrammarScore() != null ? entity.getGrammarScore() : 0;
        int readScore = entity.getReadabilityScore() != null ? entity.getReadabilityScore() : 0;
        int formScore = Math.max(40, 100 - (entity.getFormattingIssueCount() * 10));
        int profScore = Math.max(40, 100 - (entity.getWeakVerbCount() * 8 + entity.getPassiveVoiceCount() * 6));

        int overallScore = (int) Math.round((gramScore * 0.40) + (readScore * 0.30) + (formScore * 0.15) + (profScore * 0.15));

        return GrammarAnalysisDto.builder()
                .id(entity.getId())
                .resumeId(entity.getResume() != null ? entity.getResume().getId() : null)
                .resumeFileName(entity.getResume() != null ? entity.getResume().getOriginalFileName() : null)
                .grammarScore(gramScore)
                .readabilityScore(readScore)
                .formattingScore(formScore)
                .professionalWritingScore(profScore)
                .overallWritingScore(overallScore)
                .spellingErrorCount(entity.getSpellingErrorCount())
                .grammarErrorCount(entity.getGrammarErrorCount())
                .passiveVoiceCount(entity.getPassiveVoiceCount())
                .repeatedWordCount(entity.getRepeatedWordCount())
                .longSentenceCount(entity.getLongSentenceCount())
                .weakVerbCount(entity.getWeakVerbCount())
                .formattingIssueCount(entity.getFormattingIssueCount())
                .spellingErrors(parseJsonList(entity.getSpellingErrors()))
                .grammarErrors(parseJsonList(entity.getGrammarErrors()))
                .weakVerbs(parseJsonList(entity.getWeakVerbs()))
                .passiveVoiceInstances(parseJsonList(entity.getPassiveVoiceInstances()))
                .suggestions(parseJsonList(entity.getSuggestions()))
                .analysisDate(entity.getAnalysisDate())
                .build();
    }

    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
