package com.church.website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "worship_schedule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorshipSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    /** 예배 구분: SUNDAY(주일) / WEEKDAY(주중) */
    @Column(length = 20, nullable = false)
    private String category;

    @Column(length = 20)
    private String serviceTime;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    private Integer displayOrder;

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
        if (displayOrder == null) displayOrder = 0;
        if (category == null) category = "SUNDAY";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
