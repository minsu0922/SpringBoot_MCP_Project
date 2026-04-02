package com.church.website.service;

import com.church.website.entity.NewFamily;
import com.church.website.repository.NewFamilyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 새가족 등록 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewFamilyService {

    private final NewFamilyRepository newFamilyRepository;

    /** 전체 목록 조회 */
    public List<NewFamily> getAll() {
        return newFamilyRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 미확인 건수 */
    public long getUncheckedCount() {
        return newFamilyRepository.countByCheckedFalse();
    }

    /** 새가족 등록 */
    @Transactional
    public NewFamily register(NewFamily newFamily) {
        log.info("새가족 등록: {}", newFamily.getName());
        return newFamilyRepository.save(newFamily);
    }

    /** 확인 처리 */
    @Transactional
    public void check(Long id) {
        NewFamily nf = newFamilyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("새가족 정보를 찾을 수 없습니다."));
        nf.setChecked(true);
        newFamilyRepository.save(nf);
        log.info("새가족 확인 처리: {}", nf.getName());
    }

    /** 삭제 */
    @Transactional
    public void delete(Long id) {
        NewFamily nf = newFamilyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("새가족 정보를 찾을 수 없습니다."));
        log.info("새가족 삭제: {}", nf.getName());
        newFamilyRepository.delete(nf);
    }
}
