package com.airesume.analyzer.service.grammar;

import com.airesume.analyzer.dto.GrammarAnalysisDto;

public interface GrammarAnalysisService {

    GrammarAnalysisDto analyzeGrammar(Long resumeId, String currentUserEmail);

    GrammarAnalysisDto getGrammarAnalysisByResumeId(Long resumeId, String currentUserEmail);

    void deleteGrammarAnalysis(Long id, String currentUserEmail);
}
