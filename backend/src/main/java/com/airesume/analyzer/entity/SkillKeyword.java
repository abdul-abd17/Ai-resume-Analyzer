package com.airesume.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skill_keywords", uniqueConstraints = {
    @UniqueConstraint(columnNames = "keyword")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "keyword", nullable = false, unique = true)
    private String keyword;

    @Column(name = "weight", nullable = false)
    @Builder.Default
    private Integer weight = 1;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = false;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "ACTIVE";
}
