package com.church.website.repository;

import com.church.website.entity.Sermon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 설교 영상 데이터 접근 Repository.
 *
 * <p>JpaRepository의 기본 CRUD 외에 설교 날짜 기준 최신 N건 조회와
 * 성경 본문 검색(SermonRepositoryCustom)을 제공한다.
 *
 * <p>성경 본문 검색은 대소문자 무시 부분 일치가 필요하고 페이지네이션도 함께 처리해야 하므로
 * 메서드명 쿼리 대신 QueryDSL(SermonRepositoryCustom)을 사용한다.
 */
public interface SermonRepository extends JpaRepository<Sermon, Long>, SermonRepositoryCustom {

    /**
     * 메인 페이지용 최신 설교 5건.
     * 단순 정렬이므로 메서드명 쿼리로 충분하다.
     */
    List<Sermon> findTop5ByOrderBySermonDateDesc();
}
