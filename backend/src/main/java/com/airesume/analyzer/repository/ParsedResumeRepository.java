package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.ParsedResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParsedResumeRepository extends JpaRepository<ParsedResume, Long> {

    Optional<ParsedResume> findByResumeId(Long resumeId);

    Optional<ParsedResume> findByResumeIdAndResumeUserId(Long resumeId, Long userId);

    void deleteByResumeId(Long resumeId);
}
