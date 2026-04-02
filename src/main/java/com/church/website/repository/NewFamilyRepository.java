package com.church.website.repository;

import com.church.website.entity.NewFamily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 새가족 등록 레포지토리
 */
public interface NewFamilyRepository extends JpaRepository<NewFamily, Long> {

    /** 전체 목록 (최신순) */
    List<NewFamily> findAllByOrderByCreatedAtDesc();

    /** 미확인 건수 */
    long countByCheckedFalse();
}
