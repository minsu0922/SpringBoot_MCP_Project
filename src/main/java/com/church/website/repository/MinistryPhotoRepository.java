package com.church.website.repository;

import com.church.website.entity.MinistryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 사역 소개 사진 레포지토리
 */
@Repository
public interface MinistryPhotoRepository extends JpaRepository<MinistryPhoto, Long> {

    /** 활성화된 사진 전체 (표시 순서) */
    List<MinistryPhoto> findByIsActiveTrueOrderByDisplayOrderAsc();

    /** 카테고리별 활성화 사진 */
    List<MinistryPhoto> findByCategoryAndIsActiveTrueOrderByDisplayOrderAsc(String category);

    /** 전체 목록 (표시 순서) */
    List<MinistryPhoto> findAllByOrderByDisplayOrderAsc();
}
