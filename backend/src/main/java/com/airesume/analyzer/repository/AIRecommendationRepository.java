package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.AIRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {

    Optional<AIRecommendation> findTopByResumeIdOrderByCreatedAtDesc(Long resumeId);

    void deleteByResumeId(Long resumeId);
}
