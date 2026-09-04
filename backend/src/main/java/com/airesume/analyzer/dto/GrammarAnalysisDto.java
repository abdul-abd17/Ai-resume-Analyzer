package com.airesume.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarAnalysisDto {

    private Long id;
    private Long resumeId;
    private String resumeFileName;

    private Integer grammarScore;
    private Integer readabilityScore;
    private Integer formattingScore;
    private Integer professionalWritingScore;
    private Integer overallWritingScore;

    private Integer spellingErrorCount;
    private Integer grammarErrorCount;
    private Integer passiveVoiceCount;
    private Integer repeatedWordCount;
    private Integer longSentenceCount;
    private Integer weakVerbCount;
    private Integer formattingIssueCount;

    private List<String> spellingErrors;
    private List<String> grammarErrors;
    private List<String> weakVerbs;
    private List<String> passiveVoiceInstances;
    private List<String> suggestions;

    private LocalDateTime analysisDate;
}
