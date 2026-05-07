package com.church.website.service;

import com.church.website.entity.Bulletin;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.BulletinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 주보(週報) 비즈니스 로직 서비스.
 *
 * <p>주보 CRUD를 담당한다.
 * 별도의 복잡한 비즈니스 규칙 없이 Repository에 위임하는 단순 서비스이지만,
 * 컨트롤러와 Repository 사이의 레이어를 유지하여 향후 확장을 용이하게 한다.
 *
 * <p>주보는 예배 날짜(worshipDate)를 기준으로 내림차순 정렬한다.
 * 신규 등록 폼에서 "직전 주보 값 인계" 기능을 위해 {@link #getLatest()}를 제공한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BulletinService {

    private final BulletinRepository bulletinRepository;

    /**
     * 주보 목록을 예배 날짜 내림차순·페이지네이션으로 반환한다.
     *
     * @param pageable 페이지 번호·크기·정렬 정보
     * @return 페이지 객체 (현재 페이지 데이터 + 전체 건수 포함)
     */
    public Page<Bulletin> getAll(Pageable pageable) {
        return bulletinRepository.findAllByOrderByWorshipDateDesc(pageable);
    }

    /**
     * ID로 단건 조회.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    public Bulletin getById(Long id) {
        return bulletinRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("주보를 찾을 수 없습니다."));
    }

    /**
     * 가장 최근 주보 1건을 반환한다.
     * 신규 등록 폼에서 사회자·찬송 번호처럼 반복되는 값을 자동으로 채울 때 사용한다.
     *
     * @return 최신 주보 Optional — 주보가 하나도 없으면 Optional.empty()
     */
    public Optional<Bulletin> getLatest() {
        return bulletinRepository.findTopByOrderByWorshipDateDesc();
    }

    /**
     * 주보 저장 (등록/수정 공용).
     * id가 있으면 UPDATE, 없으면 INSERT로 JPA가 자동 처리한다.
     */
    public Bulletin save(Bulletin bulletin) {
        boolean isNew = bulletin.getId() == null;
        Bulletin saved = bulletinRepository.save(bulletin);
        log.info("주보 {} 완료: id={}, 예배날짜={}", isNew ? "등록" : "수정", saved.getId(), saved.getWorshipDate());
        return saved;
    }

    /** 주보 삭제. */
    public void delete(Long id) {
        log.info("주보 삭제: id={}", id);
        bulletinRepository.deleteById(id);
    }
}
