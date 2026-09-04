package com.airesume.analyzer.repository;

import com.airesume.analyzer.entity.SkillKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillKeywordRepository extends JpaRepository<SkillKeyword, Long> {

    Optional<SkillKeyword> findByKeywordIgnoreCase(String keyword);

    List<SkillKeyword> findByStatus(String status);

    List<SkillKeyword> findByCategory(String category);
}
