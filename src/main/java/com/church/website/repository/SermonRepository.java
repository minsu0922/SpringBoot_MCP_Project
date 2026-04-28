package com.church.website.repository;

import com.church.website.entity.Sermon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SermonRepository extends JpaRepository<Sermon, Long>, SermonRepositoryCustom {

    // 메인 화면 최신 5개 — 단순 정렬이므로 메서드명 쿼리로 충분
    List<Sermon> findTop5ByOrderBySermonDateDesc();
}
