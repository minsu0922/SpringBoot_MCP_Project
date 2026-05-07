package com.church.website.repository;

import com.church.website.entity.Sermon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 설교 커스텀 검색 인터페이스.
 *
 * <p>성경 본문 키워드 검색 + 페이지네이션을 선언한다.
 * 실제 구현은 {@link SermonRepositoryImpl}에서 QueryDSL로 작성된다.
 */
public interface SermonRepositoryCustom {

    /**
     * 성경 본문으로 설교를 검색한다 (페이지네이션 포함).
     *
     * @param biblePassage 성경 본문 검색어 (비어있으면 전체 조회, 대소문자 무시 부분 일치)
     * @param pageable     페이지 번호·크기·정렬 정보
     * @return 필터링된 설교 목록 (페이지 정보 포함)
     */
    Page<Sermon> searchSermons(String biblePassage, Pageable pageable);
}
