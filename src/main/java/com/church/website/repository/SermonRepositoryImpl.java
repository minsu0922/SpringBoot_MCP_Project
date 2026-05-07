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

/**
 * 설교 커스텀 검색 QueryDSL 구현체.
 *
 * <p>SermonRepositoryCustom을 구현하며, 성경 본문 키워드 검색을 대소문자 무시 부분 일치로 처리한다.
 * containsIgnoreCase()는 QueryDSL이 DB의 LOWER() 함수를 활용하도록 SQL을 생성한다.
 *
 * <p>클래스명은 반드시 "{인터페이스명}Impl" 이어야 Spring Data JPA가 자동 인식한다.
 */
@RequiredArgsConstructor
public class SermonRepositoryImpl implements SermonRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Sermon> searchSermons(String biblePassage, Pageable pageable) {
        QSermon s = QSermon.sermon;
        BooleanBuilder builder = new BooleanBuilder();

        // 검색어가 있을 때만 조건 추가 — 없으면 전체 조회
        if (biblePassage != null && !biblePassage.isBlank()) {
            // containsIgnoreCase: "고린도" 검색 시 "고린도전서", "고린도후서" 모두 히트
            builder.and(s.biblePassage.containsIgnoreCase(biblePassage));
        }

        // 전체 건수 조회 (총 페이지 수 계산용)
        long total = queryFactory
                .select(s.count())
                .from(s)
                .where(builder)
                .fetchOne();

        // 현재 페이지 데이터 (설교 날짜 내림차순)
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
