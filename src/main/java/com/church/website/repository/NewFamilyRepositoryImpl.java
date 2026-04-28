package com.church.website.repository;

import com.church.website.entity.NewFamily;
import com.church.website.entity.QNewFamily;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class NewFamilyRepositoryImpl implements NewFamilyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<NewFamily> searchByKeywordAndStatus(String keyword, String status, Pageable pageable) {
        QNewFamily nf = QNewFamily.newFamily;
        BooleanBuilder builder = new BooleanBuilder();

        // 이름 키워드 — 값이 있을 때만 조건 추가
        if (keyword != null && !keyword.isEmpty()) {
            builder.and(nf.name.contains(keyword));
        }

        // 확인 상태 필터 — "checked"/"unchecked"일 때만 조건 추가
        if ("unchecked".equals(status)) {
            builder.and(nf.checked.isFalse());
        } else if ("checked".equals(status)) {
            builder.and(nf.checked.isTrue());
        }

        // 전체 건수 (페이지네이션 계산용)
        long total = queryFactory
                .select(nf.count())
                .from(nf)
                .where(builder)
                .fetchOne();

        // 현재 페이지 데이터
        List<NewFamily> content = queryFactory
                .selectFrom(nf)
                .where(builder)
                .orderBy(nf.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }
}
