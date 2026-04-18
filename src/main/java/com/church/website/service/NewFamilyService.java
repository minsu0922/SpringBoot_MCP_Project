package com.church.website.service;

import com.church.website.entity.NewFamily;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.NewFamilyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @Transactional(readOnly = true)
    public List<NewFamily> getAll() {
        return newFamilyRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 검색 + 필터 + 페이지네이션 */
    @Transactional(readOnly = true)
    public Page<NewFamily> search(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newFamilyRepository.searchByKeywordAndStatus(keyword, status, pageable);
    }

    /** 단건 조회 */
    @Transactional(readOnly = true)
    public NewFamily getById(Long id) {
        return newFamilyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("새가족 정보를 찾을 수 없습니다."));
    }

    /** 미확인 건수 */
    @Transactional(readOnly = true)
    public long getUncheckedCount() {
        return newFamilyRepository.countByCheckedFalse();
    }

    /** 확인 건수 */
    @Transactional(readOnly = true)
    public long getCheckedCount() {
        return newFamilyRepository.countByChecked(true);
    }

    /** 새가족 등록 */
    @Transactional
    public NewFamily register(NewFamily newFamily) {
        log.info("새가족 등록: {}", newFamily.getName());
        return newFamilyRepository.save(newFamily);
    }

    /** 확인 처리 (checked = true) */
    @Transactional
    public void check(Long id) {
        NewFamily nf = newFamilyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("새가족 정보를 찾을 수 없습니다."));
        nf.setChecked(true);
        newFamilyRepository.save(nf);
        log.info("새가족 확인 처리: {}", nf.getName());
    }

    /** 확인 취소 (checked = false) */
    @Transactional
    public void uncheck(Long id) {
        NewFamily nf = newFamilyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("새가족 정보를 찾을 수 없습니다."));
        nf.setChecked(false);
        newFamilyRepository.save(nf);
        log.info("새가족 확인 취소: {}", nf.getName());
    }

    /** 관리자 메모 저장 */
    @Transactional
    public void saveAdminMemo(Long id, String adminMemo) {
        NewFamily nf = newFamilyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("새가족 정보를 찾을 수 없습니다."));
        nf.setAdminMemo(adminMemo);
        newFamilyRepository.save(nf);
        log.info("새가족 관리자 메모 저장: {}", nf.getName());
    }

    /** 삭제 */
    @Transactional
    public void delete(Long id) {
        NewFamily nf = newFamilyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("새가족 정보를 찾을 수 없습니다."));
        log.info("새가족 삭제: {}", nf.getName());
        newFamilyRepository.delete(nf);
    }
}
