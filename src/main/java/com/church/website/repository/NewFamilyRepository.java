package com.church.website.repository;

import com.church.website.entity.NewFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 새가족 등록 레포지토리
 */
public interface NewFamilyRepository extends JpaRepository<NewFamily, Long> {

    /** 전체 목록 (최신순) */
    List<NewFamily> findAllByOrderByCreatedAtDesc();

    /** 미확인 건수 */
    long countByCheckedFalse();

    /** 이름 검색 + 상태 필터 (페이지네이션) */
    @Query("SELECT n FROM NewFamily n WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR n.name LIKE %:keyword%) AND " +
           "(:status IS NULL OR :status = '' OR " +
           "  (:status = 'unchecked' AND n.checked = false) OR " +
           "  (:status = 'checked'   AND n.checked = true)) " +
           "ORDER BY n.createdAt DESC")
    Page<NewFamily> searchByKeywordAndStatus(
            @Param("keyword") String keyword,
            @Param("status")  String status,
            Pageable pageable);

    /** 전체 카운트 (상태 필터) */
    long countByChecked(boolean checked);
}
