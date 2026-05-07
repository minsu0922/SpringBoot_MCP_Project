package com.church.website.repository;

import com.church.website.entity.NewFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 새가족 등록 정보 데이터 접근 Repository.
 *
 * <p>JpaRepository가 제공하는 기본 CRUD 외에 새가족 관리에 특화된 쿼리 메서드와
 * 커스텀 검색(NewFamilyRepositoryCustom)을 추가한다.
 *
 * <p>쿼리 방식 선택:
 * <ul>
 *   <li>단순 정렬·Top N·카운트 → 메서드명 쿼리</li>
 *   <li>메모만 단독 업데이트 → @Query JPQL UPDATE (엔티티 전체 로드 불필요)</li>
 *   <li>키워드 + 상태 복합 필터 → NewFamilyRepositoryCustom (QueryDSL)</li>
 * </ul>
 */
public interface NewFamilyRepository extends JpaRepository<NewFamily, Long>, NewFamilyRepositoryCustom {

    /** 전체 목록 최신순 — 관리자 대시보드용 */
    List<NewFamily> findAllByOrderByCreatedAtDesc();

    /** 미확인(checked=false) 중 최신 5건 — 대시보드 알림 목록 */
    List<NewFamily> findTop5ByCheckedFalseOrderByCreatedAtDesc();

    /** 미확인 건수 — 사이드바 뱃지 숫자 */
    long countByCheckedFalse();

    /** checked 값에 따른 건수 카운트 (확인/미확인 각각 조회) */
    long countByChecked(boolean checked);

    /**
     * 관리자 메모(adminMemo)만 직접 업데이트한다.
     * 엔티티 전체를 로드하지 않고 UPDATE 쿼리 한 번으로 처리한다.
     * @Modifying은 SELECT 외의 DML에 반드시 필요하다.
     */
    @Modifying
    @Query("UPDATE NewFamily nf SET nf.adminMemo = :memo WHERE nf.id = :id")
    void updateAdminMemo(@Param("id") Long id, @Param("memo") String memo);
}
