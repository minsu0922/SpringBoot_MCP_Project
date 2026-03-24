package com.church.website.repository;

import com.church.website.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 교회 위치 정보 리포지토리
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    /**
     * 활성화된 위치 정보 조회
     */
    Optional<Location> findFirstByIsActiveTrueOrderByIdDesc();
}
