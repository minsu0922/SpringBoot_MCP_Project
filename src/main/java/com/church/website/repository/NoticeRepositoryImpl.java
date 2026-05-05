package com.church.website.repository;

import com.church.website.entity.Notice;
import com.church.website.entity.QNotice;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notice> searchNotices(String keyword, String popupStatus) {
        QNotice n = QNotice.notice;
        return queryFactory
                .selectFrom(n)
                .where(buildCondition(n, keyword, popupStatus))
                .orderBy(n.createdAt.desc())
                .fetch();
    }

    @Override
    public Page<Notice> searchNotices(String keyword, String popupStatus, Pageable pageable) {
        QNotice n = QNotice.notice;
        BooleanBuilder builder = buildCondition(n, keyword, popupStatus);

        List<Notice> content = queryFactory
                .selectFrom(n)
                .where(builder)
                .orderBy(n.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(n.count())
                .from(n)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder buildCondition(QNotice n, String keyword, String popupStatus) {
        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null && !keyword.isEmpty()) {
            builder.and(n.title.contains(keyword));
        }
        if ("on".equals(popupStatus)) {
            builder.and(n.popup.isTrue());
        } else if ("off".equals(popupStatus)) {
            builder.and(n.popup.isFalse());
        }
        return builder;
    }
}
