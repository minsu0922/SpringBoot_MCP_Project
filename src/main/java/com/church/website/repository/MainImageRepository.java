package com.church.website.repository;

import com.church.website.entity.MainImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 메인 페이지 슬라이더 이미지 데이터 접근 Repository.
 *
 * <p>JpaRepository 기본 CRUD 외에 활성화 여부·표시 순서 기준 조회 메서드를 제공한다.
 * isActive=true인 이미지만 공개 슬라이더에 표시하며, displayOrder 오름차순으로 정렬한다.
 */
@Repository
public interface MainImageRepository extends JpaRepository<MainImage, Long> {

    /**
     * 활성화된 이미지를 표시 순서대로 반환한다.
     * 메인 페이지 슬라이더에 사용한다.
     */
    List<MainImage> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 전체 이미지를 표시 순서대로 반환한다 (활성화 여부 관계없이).
     * 관리자 목록 페이지에서 사용한다.
     */
    List<MainImage> findAllByOrderByDisplayOrderAsc();
}
