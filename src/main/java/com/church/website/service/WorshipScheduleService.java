package com.church.website.service;

import com.church.website.entity.WorshipSchedule;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.WorshipScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorshipScheduleService {

    private final WorshipScheduleRepository worshipScheduleRepository;

    @Transactional(readOnly = true)
    public List<WorshipSchedule> getActive() {
        return worshipScheduleRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<WorshipSchedule> getAll() {
        return worshipScheduleRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public WorshipSchedule getById(Long id) {
        return worshipScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("예배 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public WorshipSchedule create(WorshipSchedule schedule) {
        List<WorshipSchedule> category = byCategoryOrdered(schedule.getCategory());
        schedule.setDisplayOrder(category.isEmpty() ? 0 : category.size());
        return worshipScheduleRepository.save(schedule);
    }

    @Transactional
    public WorshipSchedule update(Long id, WorshipSchedule updated) {
        WorshipSchedule schedule = getById(id);
        schedule.setName(updated.getName());
        schedule.setCategory(updated.getCategory());
        schedule.setServiceTime(updated.getServiceTime());
        schedule.setDescription(updated.getDescription());
        schedule.setIsActive(updated.getIsActive());
        return worshipScheduleRepository.save(schedule);
    }

    @Transactional
    public void moveUp(Long id) {
        WorshipSchedule target = getById(id);
        List<WorshipSchedule> group = byCategoryOrdered(target.getCategory());
        normalize(group);
        int idx = indexOf(group, id);
        if (idx <= 0) return;
        group.get(idx).setDisplayOrder(idx - 1);
        group.get(idx - 1).setDisplayOrder(idx);
        worshipScheduleRepository.saveAll(group);
    }

    @Transactional
    public void moveDown(Long id) {
        WorshipSchedule target = getById(id);
        List<WorshipSchedule> group = byCategoryOrdered(target.getCategory());
        normalize(group);
        int idx = indexOf(group, id);
        if (idx < 0 || idx >= group.size() - 1) return;
        group.get(idx).setDisplayOrder(idx + 1);
        group.get(idx + 1).setDisplayOrder(idx);
        worshipScheduleRepository.saveAll(group);
    }

    @Transactional
    public void delete(Long id) {
        worshipScheduleRepository.deleteById(id);
    }

    private List<WorshipSchedule> byCategoryOrdered(String category) {
        return worshipScheduleRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(s -> category.equals(s.getCategory()))
                .collect(Collectors.toList());
    }

    private void normalize(List<WorshipSchedule> list) {
        for (int i = 0; i < list.size(); i++) list.get(i).setDisplayOrder(i);
    }

    private int indexOf(List<WorshipSchedule> list, Long id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) return i;
        }
        return -1;
    }
}
