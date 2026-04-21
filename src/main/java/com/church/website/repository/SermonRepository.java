package com.church.website.repository;

import com.church.website.entity.Sermon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SermonRepository extends JpaRepository<Sermon, Long> {

    Page<Sermon> findByBiblePassageContainingIgnoreCase(String biblePassage, Pageable pageable);

    List<Sermon> findTop5ByOrderBySermonDateDesc();
}
