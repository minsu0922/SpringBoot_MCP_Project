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
 * 새가족 등록 엔티티.
 *
 * <p>방문자가 /new-family 폼에서 제출한 새가족 등록 정보를 저장한다.
 * 관리자는 /admin/new-family 에서 이 데이터를 조회·확인·메모·삭제할 수 있다.
 *
 * <p>필드에 선언된 @NotBlank, @Pattern은 Bean Validation 애너테이션으로,
 * 컨트롤러에서 @Valid를 사용하면 제출 시 자동으로 검증된다.
 *
 * <p>처리 흐름:
 * <ul>
 *   <li>방문자가 폼 제출 → checked=false 로 저장</li>
 *   <li>관리자가 확인 → checked=true 로 변경, adminMemo 기록</li>
 * </ul>
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

    /** 이름 — 비어있으면 등록 거부 */
    @NotBlank(message = "이름을 입력해 주세요.")
    @Column(nullable = false, length = 50)
    private String name;

    /** 연락처 — 010-1234-5678 형식 검증 */
    @NotBlank(message = "연락처를 입력해 주세요.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    @Column(nullable = false, length = 20)
    private String phone;

    /** 방문 경로 (예: 지인 소개, 인터넷 검색 등) */
    @Column(length = 100)
    private String visitRoute;

    /** 방문자가 직접 남긴 메모·문의 내용 */
    @Column(columnDefinition = "TEXT")
    private String memo;

    /** 관리자 내부 메모 — 방문자에게는 노출되지 않는 후속 처리 기록 */
    @Column(columnDefinition = "TEXT")
    private String adminMemo;

    /** 관리자 확인 여부 — false: 미확인(새 등록), true: 확인 완료 */
    @Builder.Default
    @Column(nullable = false)
    private boolean checked = false;

    /** 등록 일시 — @PrePersist로 자동 설정 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 엔티티가 처음 DB에 저장되기 직전 자동 호출 — 등록 시각을 현재 시각으로 채운다. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
