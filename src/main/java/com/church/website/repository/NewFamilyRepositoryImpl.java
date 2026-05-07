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

/**
 * 새가족 커스텀 검색 QueryDSL 구현체.
 *
 * <p>NewFamilyRepositoryCustom을 구현하며, 이름 키워드와 확인 상태 두 조건을
 * BooleanBuilder로 동적 조합하여 쿼리를 생성한다.
 *
 * <p>클래스명은 반드시 "{인터페이스명}Impl" 이어야 Spring Data JPA가 자동 인식한다.
 */
@RequiredArgsConstructor
public class NewFamilyRepositoryImpl implements NewFamilyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<NewFamily> searchByKeywordAndStatus(String keyword, String status, Pageable pageable) {
        QNewFamily nf = QNewFamily.newFamily;
        BooleanBuilder builder = new BooleanBuilder();

        // 이름 키워드 — 값이 있을 때만 부분 일치 조건 추가
        if (keyword != null && !keyword.isEmpty()) {
            builder.and(nf.name.contains(keyword));
        }

        // 확인 상태 필터 — "checked"/"unchecked"일 때만 조건 추가, 그 외엔 전체 조회
        if ("unchecked".equals(status)) {
            builder.and(nf.checked.isFalse());
        } else if ("checked".equals(status)) {
            builder.and(nf.checked.isTrue());
        }

        // 전체 건수 조회 (페이지네이션 UI의 총 페이지 수 계산에 필요)
        long total = queryFactory
                .select(nf.count())
                .from(nf)
                .where(builder)
                .fetchOne();

        // 현재 페이지에 해당하는 데이터만 조회 (최신 등록순)
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
