package com.church.website.repository;

import com.church.website.entity.QSermon;
import com.church.website.entity.Sermon;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class SermonRepositoryImpl implements SermonRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Sermon> searchSermons(String biblePassage, Pageable pageable) {
        QSermon s = QSermon.sermon;
        BooleanBuilder builder = new BooleanBuilder();

        // 성경 본문 검색어 — 비어있으면 조건 없이 전체 조회
        if (biblePassage != null && !biblePassage.isBlank()) {
            builder.and(s.biblePassage.containsIgnoreCase(biblePassage));
        }

        long total = queryFactory
                .select(s.count())
                .from(s)
                .where(builder)
                .fetchOne();

        List<Sermon> content = queryFactory
                .selectFrom(s)
                .where(builder)
                .orderBy(s.sermonDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }
}
