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

/**
 * 주보(週報) 엔티티.
 *
 * <p>매주 발행하는 교회 주보 정보를 담는다.
 * 주보는 크게 앞면(예배 식순)과 뒷면(설교 노트)으로 구성된다.
 *
 * <p>앞면 — 예배 식순:
 * <ul>
 *   <li>예배 날짜, 사회자, 찬송 번호, 성시교독문, 성경봉독 본문, 설교 제목</li>
 *   <li>찬양(praise1~4)은 찬양팀이 부르는 경배와 찬양 곡목</li>
 * </ul>
 *
 * <p>뒷면 — 설교 노트:
 * <ul>
 *   <li>들어가며(intro)·본론(body)·결론(conclusion) 세 섹션</li>
 * </ul>
 *
 * <p>@CreationTimestamp, @UpdateTimestamp 는 Hibernate가 자동으로
 * INSERT/UPDATE 시각을 채워준다. @PrePersist/@PreUpdate 대신 Hibernate 기능을 사용.
 */
@Entity
@Table(name = "bulletin")
@Getter @Setter
@NoArgsConstructor
public class Bulletin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── 예배 기본 정보 ──

    /** 예배 날짜 — HTML의 date 입력과 연동하기 위해 @DateTimeFormat 지정 */
    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate worshipDate;

    /** 성경 범위 표시 (예: "12-15절") */
    private String scriptureRange;

    // ── 찬양팀 곡목 ──
    private String praise1;
    private String praise2;
    private String praise3;
    private String praise4;

    // ── 예배 식순 담당자 및 찬송 번호 ──

    /** 사회자 이름 */
    private String emcee;

    /** 묵도 후 찬송 번호 */
    private String hymn1;

    /** 기도 전 찬송 번호 */
    private String hymn2;

    /** 설교 후 찬송 번호 */
    private String hymn3;

    /** 성시교독문(교독문) 전체 텍스트 — 길이가 길어 TEXT 타입 사용 */
    @Column(columnDefinition = "TEXT")
    private String responsiveNo;

    /** 기도 담당자 이름 */
    private String intercessor;

    // ── 성경봉독 / 설교 ──

    /** 성경봉독 본문 (예: "고린도전서 14:12-19") */
    private String biblePassage;

    /** 성경 신약 페이지 번호 */
    private String bibleNtPage;

    /** 담임목사 이름 */
    private String pastor;

    /** 설교 제목 */
    @Column(length = 500)
    private String sermonTitle;

    // ── 다음 주 예고 ──

    /** 다음 주일 날짜 표시 (예: "4/19") */
    private String nextSundayDate;

    /** 다음 주일 기도 담당자 */
    private String nextIntercessor;

    // ── 설교 노트 (주보 뒷면) ──

    /** 설교 본문 (예: "고린도전서 14:12-19") */
    private String sermonPassage;

    /** 들어가며 — 설교 도입부 */
    @Column(columnDefinition = "TEXT")
    private String introContent;

    /** 본론 — 설교 핵심 내용 */
    @Column(columnDefinition = "TEXT")
    private String bodyContent;

    /** 결론 — 설교 결말 및 적용 */
    @Column(columnDefinition = "TEXT")
    private String conclusionContent;

    // ── 메타 정보 ──

    /** Hibernate가 INSERT 시 자동으로 현재 시각을 채워준다 */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /** Hibernate가 UPDATE 시 자동으로 현재 시각을 채워준다 */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
