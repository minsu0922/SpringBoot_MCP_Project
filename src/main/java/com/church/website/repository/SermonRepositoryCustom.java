package com.church.website.repository;

import com.church.website.entity.Sermon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SermonRepositoryCustom {

    /**
     * 성경 본문 검색 + 페이지네이션
     * biblePassage가 비어있으면 전체 조회, 값이 있으면 대소문자 무시 부분 일치
     */
    Page<Sermon> searchSermons(String biblePassage, Pageable pageable);
}
