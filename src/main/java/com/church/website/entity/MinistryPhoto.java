package com.church.website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사역 소개 사진 엔티티.
 *
 * <p>사역소개 페이지(/ministry)와 메인 페이지에 표시되는 사역 사진 정보를 담는다.
 * 실제 이미지 파일은 서버 파일시스템(uploads/ministry/)에 저장하고,
 * 이 엔티티에는 해당 파일을 가리키는 URL만 저장한다.
 *
 * <p>주요 동작:
 * <ul>
 *   <li>isActive=true 인 사진만 공개 페이지에 표시된다.</li>
 *   <li>displayOrder 숫자가 낮을수록 먼저 표시된다.</li>
 *   <li>사진 삭제 시 DB 레코드와 실제 파일을 모두 제거해야 한다 (AdminController 참고).</li>
 * </ul>
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

    /** 사역 설명 (선택사항) */
    @Column(length = 500)
    private String description;

    /** 이미지 URL — "/uploads/ministry/파일명.jpg" 형태로 저장 */
    @Column(nullable = false, length = 500)
    private String photoUrl;

    /** 표시 순서 — 낮을수록 먼저 표시, 기본값 0 */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /** 활성화 여부 — false이면 공개 페이지에 표시되지 않음 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** 최초 등록 일시 — @PrePersist로 자동 설정 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 마지막 수정 일시 — @PreUpdate로 자동 갱신 */
    @Column
    private LocalDateTime updatedAt;

    /** 엔티티가 처음 DB에 저장되기 직전 자동 호출 — 등록 시각을 현재 시각으로 채운다. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** 엔티티가 DB에서 수정되기 직전 자동 호출 — 수정 시각을 현재 시각으로 갱신한다. */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
