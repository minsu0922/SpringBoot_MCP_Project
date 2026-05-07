package com.church.website.repository;

import com.church.website.entity.NewFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 새가족 커스텀 검색 인터페이스.
 *
 * <p>키워드·확인 상태라는 두 가지 조건이 선택적으로 조합되는 동적 쿼리를 선언한다.
 * 실제 구현은 {@link NewFamilyRepositoryImpl}에서 QueryDSL로 작성된다.
 */
public interface NewFamilyRepositoryCustom {

    /**
     * 이름 키워드 검색 + 확인 상태 필터 + 페이지네이션.
     *
     * @param keyword  이름 검색어 (비어있으면 조건 없음)
     * @param status   "checked"=확인 완료만, "unchecked"=미확인만, ""=전체
     * @param pageable 페이지 번호·크기·정렬 정보
     * @return 필터링된 새가족 목록 (페이지 정보 포함)
     */
    Page<NewFamily> searchByKeywordAndStatus(String keyword, String status, Pageable pageable);
}
