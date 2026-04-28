package com.church.website.repository;

import com.church.website.entity.NewFamily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewFamilyRepository extends JpaRepository<NewFamily, Long>, NewFamilyRepositoryCustom {

    // 전체 목록 (최신순) — 관리자 대시보드용
    List<NewFamily> findAllByOrderByCreatedAtDesc();

    // 대시보드 미확인 최신 5건
    List<NewFamily> findTop5ByCheckedFalseOrderByCreatedAtDesc();

    // 미확인 건수 — 사이드바 뱃지용
    long countByCheckedFalse();

    // 확인 건수
    long countByChecked(boolean checked);
}
