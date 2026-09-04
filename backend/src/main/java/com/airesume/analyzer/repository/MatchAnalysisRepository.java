package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.MatchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchAnalysisRepository extends JpaRepository<MatchAnalysis, Long> {

    Optional<MatchAnalysis> findTopByResumeIdAndJobDescriptionIdOrderByCreatedAtDesc(Long resumeId, Long jobDescriptionId);

    Optional<MatchAnalysis> findTopByResumeIdOrderByCreatedAtDesc(Long resumeId);

    List<MatchAnalysis> findByResumeUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByJobDescriptionId(Long jobDescriptionId);

    void deleteByResumeId(Long resumeId);
}
