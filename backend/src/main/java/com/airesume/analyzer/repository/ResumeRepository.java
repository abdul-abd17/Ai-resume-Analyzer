package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.Resume;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserId(Long userId);

    Page<Resume> findByUserId(Long userId, Pageable pageable);

    Page<Resume> findByUserIdAndOriginalFileNameContainingIgnoreCase(Long userId, String search, Pageable pageable);

    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resume r WHERE r.id = :id")
    Optional<Resume> findByIdWithLock(@Param("id") Long id);
}
