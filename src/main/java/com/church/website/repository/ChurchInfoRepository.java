package com.church.website.repository;

import com.church.website.entity.ChurchInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChurchInfoRepository extends JpaRepository<ChurchInfo, Long> {
    Optional<ChurchInfo> findFirstByIsActiveTrueOrderByIdDesc();
}
