package com.airesume.analyzer.service.parser;

import com.airesume.analyzer.entity.ParsedResume;
import com.airesume.analyzer.repository.ParsedResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ParsedResumePersistenceHelper {

    private final ParsedResumeRepository parsedResumeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ParsedResume saveInNewTransaction(ParsedResume parsedResume) {
        return parsedResumeRepository.saveAndFlush(parsedResume);
    }
}
