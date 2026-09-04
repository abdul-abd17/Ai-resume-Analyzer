package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.GrammarAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrammarAnalysisRepository extends JpaRepository<GrammarAnalysis, Long> {

    Optional<GrammarAnalysis> findTopByResumeIdOrderByAnalysisDateDesc(Long resumeId);

    void deleteByResumeId(Long resumeId);
}
