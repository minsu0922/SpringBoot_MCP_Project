package com.church.website.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정 클래스.
 *
 * QueryDSL은 타입 안전한 JPQL을 Java 코드로 작성할 수 있게 해주는 라이브러리다.
 * 이 클래스는 QueryDSL의 핵심 API인 JPAQueryFactory를 Spring Bean으로 등록하여
 * Repository 등 다른 컴포넌트에서 @Autowired / 생성자 주입으로 사용할 수 있게 한다.
 *
 * <pre>
 * 사용 예시 (Repository):
 *   private final JPAQueryFactory queryFactory;
 *
 *   public List<Notice> findByTitle(String title) {
 *       QNotice notice = QNotice.notice;
 *       return queryFactory
 *           .selectFrom(notice)
 *           .where(notice.title.contains(title))
 *           .fetch();
 *   }
 * </pre>
 */
@Configuration
public class QueryDslConfig {

    /**
     * JPA EntityManager.
     * @PersistenceContext를 사용하면 Spring이 현재 트랜잭션에 맞는
     * EntityManager 프록시를 자동으로 주입해 준다. (스레드 안전)
     */
    @PersistenceContext
    private EntityManager em;

    /**
     * JPAQueryFactory Bean 등록.
     *
     * JPAQueryFactory는 QueryDSL로 작성한 쿼리를 실제 JPQL로 변환·실행하는 객체다.
     * EntityManager를 인자로 받아 DB 세션과 트랜잭션 컨텍스트를 공유한다.
     *
     * @return EntityManager를 주입받은 JPAQueryFactory 인스턴스
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
