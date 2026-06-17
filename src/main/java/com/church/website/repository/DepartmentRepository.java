package com.church.website.repository;

import com.church.website.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<Department> findAllByOrderByDisplayOrderAsc();
}
