package com.church.website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사역 소개 사진 엔티티
 * 사역소개 페이지에 표시되는 사진 정보를 관리
 */
@Entity
@Table(name = "ministry_photo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinistryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사역 카테고리 (예: WORSHIP, CHILDREN, YOUTH, MISSION, COMMUNITY) */
    @Column(nullable = false, length = 50)
    private String category;

    /** 사역 제목 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 사역 설명 */
    @Column(length = 500)
    private String description;

    /** 사진 URL */
    @Column(nullable = false, length = 500)
    private String photoUrl;

    /** 표시 순서 */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /** 활성화 여부 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** 등록일시 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
