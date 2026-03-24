package com.church.website.repository;

import com.church.website.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 데이터 접근 리포지토리
 * User 엔티티의 데이터베이스 CRUD 작업을 처리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 사용자명으로 사용자 조회
     * 로그인 인증 시 사용자명을 기반으로 사용자 정보를 검색
     */
    Optional<User> findByUsername(String username);
}
