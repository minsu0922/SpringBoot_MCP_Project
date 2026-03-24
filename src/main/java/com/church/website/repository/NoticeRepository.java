package com.church.website.repository;

import com.church.website.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    /**
     * 현재 시간 기준 활성화된 팝업 공지사항 조회
     */
    @Query("SELECT n FROM Notice n WHERE n.popup = true AND n.popupStartDate <= :now AND n.popupEndDate >= :now ORDER BY n.createdAt DESC")
    List<Notice> findActivePopups(LocalDateTime now);

    /**
     * 최근 공지사항 N개 조회 (메인화면용)
     */
    List<Notice> findTop5ByOrderByCreatedAtDesc();
}
