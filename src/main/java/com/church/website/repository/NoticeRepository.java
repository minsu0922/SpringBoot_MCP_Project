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

/**
 * 공지사항 데이터 접근 Repository.
 *
 * <p>JpaRepository가 제공하는 기본 CRUD(save, findById, delete 등) 외에
 * 공지사항에 특화된 쿼리 메서드와 커스텀 검색(NoticeRepositoryCustom)을 추가한다.
 *
 * <p>쿼리 방식 선택 기준:
 * <ul>
 *   <li>단순 정렬·Top N 조회 → 메서드명 쿼리 (Spring Data JPA가 메서드명으로 SQL 자동 생성)</li>
 *   <li>날짜 범위 조건·조회수 UPDATE → @Query JPQL (메서드명이 너무 길어지거나 UPDATE 필요 시)</li>
 *   <li>키워드·팝업 상태 복합 필터 → NoticeRepositoryCustom (QueryDSL, 동적 조건 조합)</li>
 * </ul>
 */
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {

    /** 전체 목록 최신순 — 관리자 대시보드용 */
    List<Notice> findAllByOrderByCreatedAtDesc();

    /** 대시보드 최신 5개 — count 쿼리 없이 limit으로 빠르게 조회 */
    List<Notice> findTop5ByOrderByCreatedAtDesc();

    /** 공개 목록 페이지 페이지네이션 — JpaRepository 기본 제공 */
    Page<Notice> findAll(Pageable pageable);

    /** 메인 화면 최신 3개 (3열 그리드에 딱 맞춤) */
    List<Notice> findTop3ByOrderByCreatedAtDesc();

    /**
     * 현재 시각 기준 노출 중인 팝업 공지 목록.
     * popup=true이고 현재 시각이 팝업 기간 안에 있는 공지만 반환한다.
     */
    @Query("SELECT n FROM Notice n WHERE n.popup = true AND n.popupStartDate <= :now AND n.popupEndDate >= :now ORDER BY n.createdAt DESC")
    List<Notice> findActivePopups(LocalDateTime now);

    /**
     * 현재 공지사항보다 id가 작은 것 중 가장 큰 id — 이전글
     * Pageable을 사용하여 1건만 가져온다.
     */
    @Query("SELECT n FROM Notice n WHERE n.id < :id ORDER BY n.id DESC")
    List<Notice> findPrevNotice(Long id, Pageable pageable);

    /**
     * 현재 공지사항보다 id가 큰 것 중 가장 작은 id — 다음글
     * Pageable을 사용하여 1건만 가져온다.
     */
    @Query("SELECT n FROM Notice n WHERE n.id > :id ORDER BY n.id ASC")
    List<Notice> findNextNotice(Long id, Pageable pageable);

    /**
     * 조회수를 1 증가시킨다.
     * SELECT 없이 UPDATE 한 번으로 처리하여 불필요한 쿼리를 줄인다.
     * @Modifying은 SELECT 외의 DML(UPDATE, DELETE, INSERT) 쿼리에 반드시 붙여야 한다.
     */
    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
