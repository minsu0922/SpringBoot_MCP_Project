package com.church.website.repository;

import com.church.website.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {

    // 전체 목록 (최신순) — 관리자 대시보드용
    List<Notice> findAllByOrderByCreatedAtDesc();

    // 대시보드 최신 5개 — count 쿼리 없이 limit으로 빠르게 조회
    List<Notice> findTop5ByOrderByCreatedAtDesc();

    // 공개 목록 페이지 페이지네이션 — JpaRepository 기본 제공
    Page<Notice> findAll(Pageable pageable);

    // 메인 화면 최신 3개 (3열 그리드에 딱 맞춤)
    List<Notice> findTop3ByOrderByCreatedAtDesc();

    // 현재 시각 기준 노출 중인 팝업 공지 — 날짜 범위 조건은 JPQL이 간결
    @Query("SELECT n FROM Notice n WHERE n.popup = true AND n.popupStartDate <= :now AND n.popupEndDate >= :now ORDER BY n.createdAt DESC")
    List<Notice> findActivePopups(LocalDateTime now);

    // 이전/다음 글 — 단순 id 비교 JPQL
    @Query("SELECT n FROM Notice n WHERE n.id < :id ORDER BY n.id DESC")
    List<Notice> findPrevNotice(Long id, Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.id > :id ORDER BY n.id ASC")
    List<Notice> findNextNotice(Long id, Pageable pageable);

    // 조회수 증가 — SELECT 없이 UPDATE 한 번으로 처리
    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
