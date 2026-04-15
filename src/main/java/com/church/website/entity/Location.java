package com.church.website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 교회 위치 정보 엔티티
 * 교회의 주소, 연락처, 교통편 정보를 관리
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

    @Column(nullable = false, length = 100)
    private String churchName;

    @Column(nullable = false, length = 200)
    private String address;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String fax;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String subwayInfo;

    @Column(columnDefinition = "TEXT")
    private String busInfo;

    @Column(columnDefinition = "TEXT")
    private String carInfo;

    @Column(columnDefinition = "TEXT")
    private String parkingInfo;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (churchName == null) churchName = "샘물교회";
        if (address == null) address = "서울특별시 강동구 양재대로 1371 4층 406호";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
