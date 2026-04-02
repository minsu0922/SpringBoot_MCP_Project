package com.church.website.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 새가족 등록 엔티티
 */
@Entity
@Table(name = "new_family")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewFamily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이름 */
    @NotBlank(message = "이름을 입력해 주세요.")
    @Column(nullable = false, length = 50)
    private String name;

    /** 연락처 */
    @NotBlank(message = "연락처를 입력해 주세요.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    @Column(nullable = false, length = 20)
    private String phone;

    /** 방문 경로 */
    @Column(length = 100)
    private String visitRoute;

    /** 메모 / 문의 내용 */
    @Column(columnDefinition = "TEXT")
    private String memo;

    /** 확인 여부 (관리자 처리 상태) */
    @Column(nullable = false)
    private boolean checked = false;

    /** 등록 일시 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
