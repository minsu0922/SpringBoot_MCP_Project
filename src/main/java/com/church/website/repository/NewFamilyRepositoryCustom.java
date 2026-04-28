package com.church.website.repository;

import com.church.website.entity.NewFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewFamilyRepositoryCustom {

    /**
     * 이름 키워드 검색 + 확인 상태 필터 + 페이지네이션
     * keyword, status 모두 선택적 — 비어있으면 해당 조건 무시
     */
    Page<NewFamily> searchByKeywordAndStatus(String keyword, String status, Pageable pageable);
}
