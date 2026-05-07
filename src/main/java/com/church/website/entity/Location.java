package com.church.website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 교회 위치·연락처 정보 엔티티.
 *
 * <p>오시는길 페이지(/location)와 메인 페이지 하단에 표시되는
 * 교회 주소, 연락처, 교통편 안내 정보를 담는다.
 *
 * <p>운영 방식:
 * <ul>
 *   <li>레코드는 단일 행(isActive=true)으로 관리한다. LocationService.save()가
 *       기존 활성 레코드를 찾아 업데이트하는 upsert 방식을 사용한다.</li>
 *   <li>latitude/longitude는 카카오맵 API에서 지도 핀을 표시하는 데 사용된다.</li>
 * </ul>
 */
@Entity
@Table(name = "location")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 교회 이름 (기본값: "샘물교회") */
    @Column(nullable = false, length = 100)
    private String churchName;

    /** 교회 주소 (기본값: "서울특별시 강동구 ...") */
    @Column(nullable = false, length = 200)
    private String address;

    /** 카카오맵 지도 핀 위도 (없으면 지도에 핀 미표시) */
    @Column
    private Double latitude;

    /** 카카오맵 지도 핀 경도 */
    @Column
    private Double longitude;

    /** 교회 대표 전화번호 */
    @Column(length = 20)
    private String phone;

    /** 팩스 번호 */
    @Column(length = 20)
    private String fax;

    /** 이메일 주소 */
    @Column(length = 100)
    private String email;

    /** 지하철 이용 안내 (Thymeleaf에서 th:utext로 줄바꿈을 HTML로 렌더링) */
    @Column(columnDefinition = "TEXT")
    private String subwayInfo;

    /** 버스 이용 안내 */
    @Column(columnDefinition = "TEXT")
    private String busInfo;

    /** 자동차 이용 안내 */
    @Column(columnDefinition = "TEXT")
    private String carInfo;

    /** 주차 안내 */
    @Column(columnDefinition = "TEXT")
    private String parkingInfo;

    /** 활성화 여부 — 단일 레코드 관리를 위해 사용 */
    @Column(nullable = false)
    private Boolean isActive;

    /** 최초 등록 일시 — @PrePersist로 자동 설정 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 마지막 수정 일시 — @PreUpdate로 자동 갱신 */
    @Column
    private LocalDateTime updatedAt;

    /** 엔티티가 처음 DB에 저장되기 직전 자동 호출 — 필수 기본값을 채운다. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (churchName == null) churchName = "샘물교회";
        if (address == null) address = "서울특별시 강동구 양재대로 1371 4층 406호";
    }

    /** 엔티티가 DB에서 수정되기 직전 자동 호출 — 수정 시각을 현재 시각으로 갱신한다. */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
