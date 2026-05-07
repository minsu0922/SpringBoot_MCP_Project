package com.church.website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 메인 페이지 슬라이더 이미지 엔티티.
 *
 * <p>메인 홈 화면 상단의 이미지 슬라이더(캐러셀)에 표시되는 이미지 정보를 담는다.
 * isActive=true 인 이미지만 공개 화면에 표시하며, displayOrder 순으로 정렬된다.
 *
 * <p>이미지 파일 자체는 이 엔티티에 저장하지 않고,
 * imageUrl 필드에 접근 가능한 URL 경로만 저장한다.
 */
@Entity
@Table(name = "main_image")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이미지 제목 (슬라이더 위에 표시) */
    @Column(nullable = false, length = 100)
    private String title;

    /** 이미지 설명 (선택사항, 부제목으로 사용) */
    @Column(length = 500)
    private String description;

    /** 이미지 URL 경로 */
    @Column(nullable = false, length = 500)
    private String imageUrl;

    /** 표시 순서 — 낮을수록 먼저 표시, 기본값 0 */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /** 활성화 여부 — false이면 슬라이더에 표시되지 않음 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** 최초 등록 일시 — @PrePersist로 자동 설정 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 마지막 수정 일시 — @PreUpdate로 자동 갱신 */
    @Column
    private LocalDateTime updatedAt;

    /** 엔티티가 처음 DB에 저장되기 직전 자동 호출 — 등록 시각 및 기본값을 채운다. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    /** 엔티티가 DB에서 수정되기 직전 자동 호출 — 수정 시각을 현재 시각으로 갱신한다. */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
