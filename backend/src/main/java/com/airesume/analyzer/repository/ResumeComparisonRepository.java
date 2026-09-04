package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.ResumeComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeComparisonRepository extends JpaRepository<ResumeComparison, Long> {

    Optional<ResumeComparison> findByOldVersionIdAndNewVersionId(Long oldVersionId, Long newVersionId);
}
