package com.church.website.repository;

import com.church.website.entity.Notice;

import java.util.List;

public interface NoticeRepositoryCustom {

    /**
     * 관리자 공지사항 검색 (제목 키워드 + 팝업 상태 필터)
     * 조건이 없으면 전체 반환, 있는 조건만 AND로 조합
     */
    List<Notice> searchNotices(String keyword, String popupStatus);
}
