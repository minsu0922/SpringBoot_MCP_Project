package com.church.website.repository;

import com.church.website.entity.Bulletin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 주보 데이터 접근 Repository.
 *
 * <p>JpaRepository 기본 CRUD 외에 주보에 특화된 두 가지 조회 메서드를 제공한다.
 * 주보는 예배 날짜(worshipDate) 기준으로 내림차순 정렬한다.
 */
public interface BulletinRepository extends JpaRepository<Bulletin, Long> {

    /**
     * 전체 주보 목록을 예배 날짜 내림차순·페이지네이션으로 반환한다.
     * 관리자 주보 목록 페이지에서 사용한다.
     */
    Page<Bulletin> findAllByOrderByWorshipDateDesc(Pageable pageable);

    /**
     * 가장 최근(예배 날짜 기준) 주보 1건을 반환한다.
     * 신규 등록 폼에서 사회자·찬송 번호 등 반복 값을 자동으로 채우는 데 사용한다.
     *
     * @return 최신 주보 Optional — 주보가 하나도 없으면 Optional.empty()
     */
    Optional<Bulletin> findTopByOrderByWorshipDateDesc();
}
