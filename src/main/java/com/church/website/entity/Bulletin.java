package com.church.website.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bulletin")
@Getter @Setter
@NoArgsConstructor
public class Bulletin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 예배 기본 정보 ──
    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate worshipDate;          // 예배 날짜

    private String scriptureRange;          // 성경 범위 표시 (예: 12-15)

    // ── 찬양 ──
    private String praise1;
    private String praise2;
    private String praise3;
    private String praise4;

    // ── 식순 ──
    private String emcee;                   // 사회자
    private String hymn1;                   // 찬송 1 (묵도 후)
    private String hymn2;                   // 찬송 2 (기도 전)
    private String hymn3;                   // 찬송 3 (설교 후)
    @Column(columnDefinition = "TEXT")
    private String responsiveNo;            // 성시교독(교독문) 텍스트
    private String intercessor;             // 기도 담당자

    // ── 성경봉독 / 설교 ──
    private String biblePassage;            // 성경봉독 본문 (예: 고전 14:12 - 19)
    private String bibleNtPage;             // 신약 페이지 번호

    private String pastor;                  // 담임목사명
    @Column(length = 500)
    private String sermonTitle;             // 설교 제목

    // ── 다음 주 안내 ──
    private String nextSundayDate;          // 다음 주일 (예: 4/19)
    private String nextIntercessor;         // 다음 주일 기도자

    // ── 설교 노트 (뒷면) ──
    private String sermonPassage;           // 설교 본문 (예: 고전 14:12 - 19)

    @Column(columnDefinition = "TEXT")
    private String introContent;            // 1. 들어가며

    @Column(columnDefinition = "TEXT")
    private String bodyContent;             // 2. 본론

    @Column(columnDefinition = "TEXT")
    private String conclusionContent;       // 3. 결론

    // ── 메타 ──
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
