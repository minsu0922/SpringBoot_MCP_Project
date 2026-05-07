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
 * 새가족 등록 비즈니스 로직 서비스.
 *
 * <p>방문자가 제출한 새가족 등록 정보의 저장과 관리자의 후속 처리(확인·메모·삭제)를 담당한다.
 *
 * <p>주요 기능:
 * <ul>
 *   <li>register: 방문자 등록 폼 제출 시 DB에 저장</li>
 *   <li>check/uncheck: 관리자 확인 상태 토글</li>
 *   <li>saveAdminMemo: JPQL UPDATE로 메모만 단독 저장 (엔티티 전체 로드 불필요)</li>
 *   <li>getUncheckedCount: 사이드바 뱃지용 미확인 건수 실시간 조회</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewFamilyService {

    private final NewFamilyRepository newFamilyRepository;

    /** 전체 목록을 최신순으로 반환한다. */
    @Transactional(readOnly = true)
    public List<NewFamily> getAll() {
        return newFamilyRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 이름 키워드·확인 상태 필터·페이지네이션을 적용한 목록 조회.
     * 조건이 없으면 전체를 최신순으로 반환한다.
     */
    @Transactional(readOnly = true)
    public Page<NewFamily> search(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newFamilyRepository.searchByKeywordAndStatus(keyword, status, pageable);
    }

    /**
     * 단건 조회.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional(readOnly = true)
    public NewFamily getById(Long id) {
        return newFamilyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("새가족 정보를 찾을 수 없습니다."));
    }

    /** 전체 등록 건수. 관리자 대시보드 통계에 사용한다. */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return newFamilyRepository.count();
    }

    /** 대시보드 알림용 — 미확인 건 중 최신 5건을 반환한다. */
    @Transactional(readOnly = true)
    public List<NewFamily> getTop5UncheckedForDashboard() {
        return newFamilyRepository.findTop5ByCheckedFalseOrderByCreatedAtDesc();
    }

    /** 미확인 건수 — 관리자 사이드바 뱃지 숫자로 표시된다. */
    @Transactional(readOnly = true)
    public long getUncheckedCount() {
        return newFamilyRepository.countByCheckedFalse();
    }

    /** 확인 완료 건수. */
    @Transactional(readOnly = true)
    public long getCheckedCount() {
        return newFamilyRepository.countByChecked(true);
    }

    /** 새가족 신규 등록 — 방문자 폼 제출 시 호출된다. */
    @Transactional
    public NewFamily register(NewFamily newFamily) {
        log.info("새가족 등록: {}", newFamily.getName());
        return newFamilyRepository.save(newFamily);
    }

    /**
     * 관리자 확인 처리 — checked 를 true로 변경한다.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional
    public void check(Long id) {
        NewFamily nf = newFamilyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("새가족 정보를 찾을 수 없습니다."));
        nf.setChecked(true);
        newFamilyRepository.save(nf);
        log.info("새가족 확인 처리: {}", nf.getName());
    }

    /**
     * 관리자 확인 취소 — checked 를 false로 되돌린다.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional
    public void uncheck(Long id) {
        NewFamily nf = newFamilyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("새가족 정보를 찾을 수 없습니다."));
        nf.setChecked(false);
        newFamilyRepository.save(nf);
        log.info("새가족 확인 취소: {}", nf.getName());
    }

    /**
     * 관리자 내부 메모만 저장.
     * 엔티티 전체를 로드하지 않고 JPQL UPDATE로 adminMemo 필드만 직접 수정한다.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional
    public void saveAdminMemo(Long id, String adminMemo) {
        if (!newFamilyRepository.existsById(id)) {
            throw new EntityNotFoundException("새가족 정보를 찾을 수 없습니다.");
        }
        newFamilyRepository.updateAdminMemo(id, adminMemo);
        log.info("새가족 관리자 메모 저장: id={}", id);
    }

    /**
     * 새가족 레코드 삭제.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional
    public void delete(Long id) {
        NewFamily nf = newFamilyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("새가족 정보를 찾을 수 없습니다."));
        log.info("새가족 삭제: {}", nf.getName());
        newFamilyRepository.delete(nf);
    }
}
