package com.church.website.repository;

import com.church.website.entity.Notice;
import com.church.website.entity.QNotice;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notice> searchNotices(String keyword, String popupStatus) {
        QNotice n = QNotice.notice;
        BooleanBuilder builder = new BooleanBuilder();

        // 제목 키워드 검색 — 값이 있을 때만 조건 추가
        if (keyword != null && !keyword.isEmpty()) {
            builder.and(n.title.contains(keyword));
        }

        // 팝업 상태 필터 — "on"/"off"일 때만 조건 추가, 비어있으면 전체
        if ("on".equals(popupStatus)) {
            builder.and(n.popup.isTrue());
        } else if ("off".equals(popupStatus)) {
            builder.and(n.popup.isFalse());
        }

        return queryFactory
                .selectFrom(n)
                .where(builder)
                .orderBy(n.createdAt.desc())
                .fetch();
    }
}
