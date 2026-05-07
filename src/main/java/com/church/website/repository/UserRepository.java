package com.church.website.repository;

import com.church.website.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 관리자 계정 데이터 접근 Repository.
 *
 * <p>JpaRepository가 제공하는 기본 CRUD에 사용자명 조회 메서드를 추가한다.
 * Spring Security의 CustomUserDetailsService와 UserService에서 사용한다.
 *
 * <p>findByUsername의 반환 타입이 Optional인 이유:
 * 존재하지 않는 사용자명으로 조회할 때 null 대신 Optional.empty()를 반환하여
 * NullPointerException 없이 안전하게 처리할 수 있다.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자명으로 계정을 조회한다.
     * 로그인 인증(CustomUserDetailsService)과 계정 중복 확인(UserService)에 사용된다.
     *
     * @param username 조회할 사용자명
     * @return 계정이 있으면 Optional<User>, 없으면 Optional.empty()
     */
    Optional<User> findByUsername(String username);
}
