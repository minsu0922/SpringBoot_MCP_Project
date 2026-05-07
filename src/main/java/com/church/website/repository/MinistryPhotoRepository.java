package com.church.website.repository;

import com.church.website.entity.MinistryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 사역 소개 사진 데이터 접근 Repository.
 *
 * <p>JpaRepository 기본 CRUD 외에 활성화 여부·카테고리·표시 순서 기준 조회 메서드를 제공한다.
 * 메서드명 쿼리만으로 필요한 조건을 모두 표현할 수 있으므로 QueryDSL 커스텀 구현은 없다.
 */
@Repository
public interface MinistryPhotoRepository extends JpaRepository<MinistryPhoto, Long> {

    /**
     * 활성화된 사진 전체를 표시 순서대로 반환한다.
     * 공개 사역소개 페이지와 메인 페이지에서 사용한다.
     */
    List<MinistryPhoto> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 특정 카테고리의 활성화된 사진을 표시 순서대로 반환한다.
     * 카테고리별 분류가 필요할 때 사용한다.
     */
    List<MinistryPhoto> findByCategoryAndIsActiveTrueOrderByDisplayOrderAsc(String category);

    /**
     * 전체 사진을 표시 순서대로 반환한다 (활성화 여부 관계없이).
     * 관리자 목록 페이지에서 사용한다.
     */
    List<MinistryPhoto> findAllByOrderByDisplayOrderAsc();
}
