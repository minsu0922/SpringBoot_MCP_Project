package com.church.website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공지사항 엔티티.
 *
 * <p>교회 공지사항 하나를 표현하는 데이터 모델이다.
 * 일반 공지 기능 외에 팝업 공지 기능을 내장하고 있어,
 * 특정 기간 동안 메인 페이지에서 팝업으로 표시할 수 있다.
 *
 * <p>팝업 조건:
 * <ul>
 *   <li>popup = true 이고</li>
 *   <li>현재 시각이 popupStartDate 이상, popupEndDate 이하일 때 메인 화면 팝업 노출</li>
 * </ul>
 *
 * <p>조회수(viewCount)는 일반 사용자가 상세 페이지에 접근할 때마다 1씩 증가한다.
 * 관리자의 편집·조회에는 증가하지 않는다.
 */
@Entity
@Table(name = "notice")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;   // 공지사항 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 공지사항 본문 (HTML 허용)

    @Column(nullable = false)
    private String author;  // 작성자 (로그인한 관리자 아이디)

    @Column(nullable = false)
    private LocalDateTime createdAt; // 최초 등록 일시 (@PrePersist로 자동 설정)

    @Column
    private LocalDateTime updatedAt; // 마지막 수정 일시 (@PreUpdate로 자동 갱신)

    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0; // 조회수 — 초기값 0

    @Column(nullable = false)
    @Builder.Default
    private boolean popup = false; // true이면 메인 페이지 팝업으로 표시

    @Column
    private LocalDateTime popupStartDate; // 팝업 시작 일시 (null이면 즉시 표시)

    @Column
    private LocalDateTime popupEndDate;   // 팝업 종료 일시 (null이면 무기한)

    /** 엔티티가 처음 DB에 저장되기 직전 자동 호출 — 등록 시각을 현재 시각으로 채운다. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (viewCount == null) {
            viewCount = 0;
        }
    }

    /** 엔티티가 DB에서 수정되기 직전 자동 호출 — 수정 시각을 현재 시각으로 갱신한다. */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
