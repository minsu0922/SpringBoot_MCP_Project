package com.spring.public_web.repository;

import com.spring.public_web.entity.MainImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 메인 이미지 리포지토리
 */
@Repository
public interface MainImageRepository extends JpaRepository<MainImage, Long> {
    
    /**
     * 활성화된 이미지를 표시 순서대로 조회
     */
    List<MainImage> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    /**
     * 모든 이미지를 표시 순서대로 조회
     */
    List<MainImage> findAllByOrderByDisplayOrderAsc();
}
