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

/**
 * 공지사항 커스텀 검색 QueryDSL 구현체.
 *
 * <p>NoticeRepositoryCustom 인터페이스를 구현하며, JPAQueryFactory를 사용해
 * 조건이 동적으로 달라지는 검색 쿼리를 타입 안전하게 작성한다.
 *
 * <p>BooleanBuilder 동작 방식:
 * 조건이 없으면 WHERE 절 자체를 생략하고 (= 전체 조회),
 * 조건이 하나씩 추가될 때마다 AND로 연결된다.
 * 예: keyword="예배" + popupStatus="on" → WHERE title LIKE '%예배%' AND popup = true
 *
 * <p>클래스명 규칙: Spring Data JPA는 "{인터페이스명}Impl" 패턴으로
 * 자동 인식하므로 반드시 NoticeRepositoryImpl 이어야 한다.
 */
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /** 전체 목록 반환 (페이지네이션 없음). */
    @Override
    public List<Notice> searchNotices(String keyword, String popupStatus) {
        QNotice n = QNotice.notice; // QueryDSL이 컴파일 타임에 자동 생성하는 타입 안전 클래스
        return queryFactory
                .selectFrom(n)
                .where(buildCondition(n, keyword, popupStatus))
                .orderBy(n.createdAt.desc())
                .fetch();
    }

    /** 페이지네이션 포함 반환 — 전체 건수와 현재 페이지 데이터를 한 번에 제공한다. */
    @Override
    public Page<Notice> searchNotices(String keyword, String popupStatus, Pageable pageable) {
        QNotice n = QNotice.notice;
        BooleanBuilder builder = buildCondition(n, keyword, popupStatus);

        // 현재 페이지에 해당하는 데이터만 조회
        List<Notice> content = queryFactory
                .selectFrom(n)
                .where(builder)
                .orderBy(n.createdAt.desc())
                .offset(pageable.getOffset())  // 건너뛸 행 수 (page * size)
                .limit(pageable.getPageSize()) // 한 페이지 당 최대 건수
                .fetch();

        // 전체 건수 조회 (페이지네이션 UI의 총 페이지 수 계산에 필요)
        Long total = queryFactory
                .select(n.count())
                .from(n)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /**
     * 검색 조건을 BooleanBuilder로 조합하여 반환한다.
     * 각 조건은 값이 있을 때만 추가되므로 모두 선택적이다.
     */
    private BooleanBuilder buildCondition(QNotice n, String keyword, String popupStatus) {
        BooleanBuilder builder = new BooleanBuilder();
        // 키워드가 있으면 제목에 부분 일치 조건 추가
        if (keyword != null && !keyword.isEmpty()) {
            builder.and(n.title.contains(keyword));
        }
        // 팝업 상태가 명시된 경우에만 조건 추가
        if ("on".equals(popupStatus)) {
            builder.and(n.popup.isTrue());
        } else if ("off".equals(popupStatus)) {
            builder.and(n.popup.isFalse());
        }
        return builder;
    }
}
