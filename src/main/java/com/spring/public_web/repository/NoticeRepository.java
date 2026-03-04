package com.spring.public_web.repository;

import com.spring.public_web.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 공지사항 리포지토리
 */
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    /**
     * 최신 공지사항 순으로 조회
     */
    List<Notice> findAllByOrderByCreatedAtDesc();
}
