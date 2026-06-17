package com.church.website.service;

import com.church.website.entity.Department;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<Department> getActive() {
        return departmentRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<Department> getAll() {
        return departmentRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("부서를 찾을 수 없습니다."));
    }

    @Transactional
    public Department create(Department department) {
        List<Department> all = departmentRepository.findAllByOrderByDisplayOrderAsc();
        department.setDisplayOrder(all.isEmpty() ? 0 : all.size());
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(Long id, Department updated) {
        Department dept = getById(id);
        dept.setName(updated.getName());
        dept.setWorshipTime(updated.getWorshipTime());
        dept.setLocation(updated.getLocation());
        dept.setStaffInCharge(updated.getStaffInCharge());
        dept.setDescription(updated.getDescription());
        dept.setIsActive(updated.getIsActive());
        return departmentRepository.save(dept);
    }

    @Transactional
    public void moveUp(Long id) {
        List<Department> all = departmentRepository.findAllByOrderByDisplayOrderAsc();
        normalize(all);
        int idx = indexOf(all, id);
        if (idx <= 0) return;
        all.get(idx).setDisplayOrder(idx - 1);
        all.get(idx - 1).setDisplayOrder(idx);
        departmentRepository.saveAll(all);
    }

    @Transactional
    public void moveDown(Long id) {
        List<Department> all = departmentRepository.findAllByOrderByDisplayOrderAsc();
        normalize(all);
        int idx = indexOf(all, id);
        if (idx < 0 || idx >= all.size() - 1) return;
        all.get(idx).setDisplayOrder(idx + 1);
        all.get(idx + 1).setDisplayOrder(idx);
        departmentRepository.saveAll(all);
    }

    @Transactional
    public void delete(Long id) {
        departmentRepository.deleteById(id);
    }

    private void normalize(List<Department> list) {
        for (int i = 0; i < list.size(); i++) list.get(i).setDisplayOrder(i);
    }

    private int indexOf(List<Department> list, Long id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) return i;
        }
        return -1;
    }
}
