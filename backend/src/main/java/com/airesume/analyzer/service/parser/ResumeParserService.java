package com.airesume.analyzer.service.parser;

import com.airesume.analyzer.dto.ParsedResumeDto;
import com.airesume.analyzer.entity.ParsedResume;

public interface ResumeParserService {

    ParsedResumeDto parseResume(Long resumeId, String currentUserEmail);

    ParsedResume getOrParseEntity(Long resumeId, String currentUserEmail);

    ParsedResumeDto getParsedResume(Long resumeId, String currentUserEmail);

    String getRawText(Long resumeId, String currentUserEmail);
}
