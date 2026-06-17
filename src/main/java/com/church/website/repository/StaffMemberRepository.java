package com.church.website.repository;

import com.church.website.entity.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {
    List<StaffMember> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<StaffMember> findAllByOrderByDisplayOrderAsc();
}
