package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {

    Optional<AnalysisReport> findTopByResumeIdOrderByAnalysisDateDesc(Long resumeId);

    List<AnalysisReport> findByResumeUserIdOrderByAnalysisDateDesc(Long userId);

    void deleteByResumeId(Long resumeId);
}
