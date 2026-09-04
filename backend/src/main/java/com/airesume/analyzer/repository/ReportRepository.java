package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByResumeUserIdOrderByGeneratedDateDesc(Long userId);

    List<Report> findByResumeIdOrderByGeneratedDateDesc(Long resumeId);
}
