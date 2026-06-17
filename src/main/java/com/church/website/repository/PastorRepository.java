package com.church.website.repository;

import com.church.website.entity.Pastor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PastorRepository extends JpaRepository<Pastor, Long> {
    Optional<Pastor> findFirstByIsActiveTrueOrderByIdDesc();
}
