package com.church.website.repository;

import com.church.website.entity.WorshipSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorshipScheduleRepository extends JpaRepository<WorshipSchedule, Long> {
    List<WorshipSchedule> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<WorshipSchedule> findAllByOrderByDisplayOrderAsc();
}
