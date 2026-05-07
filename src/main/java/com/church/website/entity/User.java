package com.church.website.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 관리자 계정 엔티티.
 *
 * <p>샘물교회 웹사이트 관리자 로그인에 사용하는 계정 정보를 담는다.
 * Spring Security의 CustomUserDetailsService가 이 엔티티를 조회하여 인증에 활용한다.
 *
 * <p>주의사항:
 * <ul>
 *   <li>password 필드는 BCrypt로 해시된 값이 저장된다. 평문 비밀번호를 저장하지 않는다.</li>
 *   <li>role 필드는 "ROLE_ADMIN" 형태로 저장해야 Spring Security가 올바르게 인식한다.</li>
 *   <li>enabled=false 이면 로그인이 차단된다 (계정 비활성화).</li>
 * </ul>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB의 AUTO_INCREMENT 사용
    private Long id;

    @Column(nullable = false, unique = true) // 중복 아이디 불가
    private String username;

    @Column(nullable = false) // BCrypt 해시값이 저장됨 (평문 저장 금지)
    private String password;

    @Column(nullable = false) // "ROLE_ADMIN" 고정값으로 사용
    private String role;

    @Column(nullable = false)
    private boolean enabled = true; // false이면 로그인 불가 (비활성화 계정)

    @Column(updatable = false) // 최초 생성 후 수정 불가 — @PrePersist로 자동 설정
    private LocalDateTime createdAt;

    /** 엔티티가 처음 DB에 저장되기 직전 자동 호출 — 등록 시각을 현재 시각으로 채운다. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
