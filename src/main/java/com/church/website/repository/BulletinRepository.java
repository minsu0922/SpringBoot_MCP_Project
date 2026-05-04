package com.church.website.repository;

import com.church.website.entity.Bulletin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BulletinRepository extends JpaRepository<Bulletin, Long> {
    Page<Bulletin> findAllByOrderByWorshipDateDesc(Pageable pageable);
    Optional<Bulletin> findTopByOrderByWorshipDateDesc();
}
