package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {

    List<ResumeVersion> findByResumeUserIdOrderByVersionNumberDesc(Long userId);

    List<ResumeVersion> findByResumeIdOrderByVersionNumberDesc(Long resumeId);

    Optional<ResumeVersion> findTopByResumeIdOrderByVersionNumberDesc(Long resumeId);

    @Query("SELECT MAX(v.versionNumber) FROM ResumeVersion v WHERE v.resume.user.id = :userId")
    Integer findMaxVersionNumberByUserId(Long userId);
}
