package com.church.website.repository;

import com.church.website.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 공지사항 커스텀 검색 인터페이스.
 *
 * <p>Spring Data JPA의 메서드명 쿼리로 표현하기 어려운 동적 조건 검색을 선언한다.
 * 실제 구현은 {@link NoticeRepositoryImpl}에서 QueryDSL로 작성된다.
 *
 * <p>QueryDSL 커스텀 Repository 3파일 구조:
 * <ul>
 *   <li>NoticeRepositoryCustom  : 인터페이스 — 메서드 선언 (이 파일)</li>
 *   <li>NoticeRepositoryImpl    : 구현체 — JPAQueryFactory로 동적 쿼리 작성</li>
 *   <li>NoticeRepository        : 위 둘을 통합 — extends JpaRepository + NoticeRepositoryCustom</li>
 * </ul>
 */
public interface NoticeRepositoryCustom {

    /**
     * 키워드·팝업 상태로 공지사항을 필터링한다 (전체 목록).
     *
     * @param keyword     제목 검색 키워드 (비어있으면 조건 없음)
     * @param popupStatus 팝업 상태 필터 ("on"=팝업만, "off"=팝업 제외, ""=전체)
     */
    List<Notice> searchNotices(String keyword, String popupStatus);

    /**
     * 키워드·팝업 상태로 공지사항을 필터링한다 (페이지네이션 포함).
     *
     * @param keyword     제목 검색 키워드 (비어있으면 조건 없음)
     * @param popupStatus 팝업 상태 필터 ("on"=팝업만, "off"=팝업 제외, ""=전체)
     * @param pageable    페이지 번호·크기·정렬 정보
     */
    Page<Notice> searchNotices(String keyword, String popupStatus, Pageable pageable);
}
