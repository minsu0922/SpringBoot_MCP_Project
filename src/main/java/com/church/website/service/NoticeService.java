package com.church.website.service;

import com.church.website.entity.Notice;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 비즈니스 로직 서비스.
 *
 * <p>공지사항 CRUD와 조회 관련 모든 로직을 처리한다.
 * 컨트롤러는 이 서비스에 작업을 위임하고, DB 접근은 NoticeRepository에 위임한다.
 *
 * <p>조회수 처리 방식:
 * <ul>
 *   <li>일반 사용자용 {@link #getNoticeById}: UPDATE 한 번으로 조회수를 증가시킨 뒤 조회한다.
 *       SELECT + UPDATE 두 번 대신 UPDATE + SELECT 순서로 처리하여 불필요한 쿼리를 줄인다.</li>
 *   <li>관리자용 {@link #getNoticeByIdNoCount}: 조회수 증가 없이 그냥 조회한다.</li>
 * </ul>
 *
 * <p>@Transactional(readOnly=true): 읽기 전용 트랜잭션은 DB에 불필요한 쓰기 잠금을 걸지 않아
 * 성능이 좋고, JPA의 변경 감지(dirty checking)도 비활성화된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /** 전체 공지사항 목록을 최신순으로 반환한다. */
    @Transactional(readOnly = true)
    public List<Notice> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 관리자 검색 — 키워드/팝업 상태 조건을 QueryDSL로 조합해 DB에서 바로 필터링한다. */
    @Transactional(readOnly = true)
    public List<Notice> searchNotices(String keyword, String popupStatus) {
        return noticeRepository.searchNotices(keyword, popupStatus);
    }

    /** 관리자 검색 (페이지네이션 포함). */
    @Transactional(readOnly = true)
    public Page<Notice> searchNoticesPaged(String keyword, String popupStatus, Pageable pageable) {
        return noticeRepository.searchNotices(keyword, popupStatus, pageable);
    }

    /** 전체 공지사항 건수. 대시보드 통계에 사용한다. */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return noticeRepository.count();
    }

    /** 공개 목록 페이지용 — 페이지네이션 적용. */
    @Transactional(readOnly = true)
    public Page<Notice> getNoticesPaged(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    /** 메인 페이지용 최신 공지사항 3건 (3열 그리드에 딱 맞춤). */
    @Transactional(readOnly = true)
    public List<Notice> getRecentNotices() {
        return noticeRepository.findTop3ByOrderByCreatedAtDesc();
    }

    /** 관리자 대시보드용 최신 공지사항 5건. */
    @Transactional(readOnly = true)
    public List<Notice> getTop5RecentNotices() {
        return noticeRepository.findTop5ByOrderByCreatedAtDesc();
    }

    /**
     * 현재 시각 기준으로 팝업 표시 기간(popupStartDate ~ popupEndDate) 안에 있는 공지사항 조회.
     * 메인 페이지 진입 시 팝업을 띄우는 데 사용한다.
     */
    @Transactional(readOnly = true)
    public List<Notice> getActivePopups() {
        return noticeRepository.findActivePopups(LocalDateTime.now());
    }

    /**
     * 공지사항 상세 조회 (일반 사용자용).
     * @Modifying UPDATE 한 번으로 조회수를 증가시킨 뒤 SELECT — SELECT+UPDATE 두 번 대신 효율적 처리.
     *
     * @throws EntityNotFoundException 해당 id의 공지사항이 존재하지 않으면 발생
     */
    @Transactional
    public Notice getNoticeById(Long id) {
        noticeRepository.incrementViewCount(id);
        return noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
    }

    /**
     * 공지사항 조회 (관리자용) — 조회수를 증가시키지 않는다.
     *
     * @throws EntityNotFoundException 해당 id의 공지사항이 존재하지 않으면 발생
     */
    @Transactional(readOnly = true)
    public Notice getNoticeByIdNoCount(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
    }

    /** 공지사항 신규 등록. */
    @Transactional
    public Notice createNotice(Notice notice) {
        log.info("공지사항 등록: {}", notice.getTitle());
        return noticeRepository.save(notice);
    }

    /**
     * 공지사항 수정.
     * 제목·본문·팝업 설정만 업데이트하고 작성자·등록일은 유지한다.
     *
     * @throws EntityNotFoundException 해당 id의 공지사항이 존재하지 않으면 발생
     */
    @Transactional
    public Notice updateNotice(Long id, Notice updatedNotice) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
        notice.setTitle(updatedNotice.getTitle());
        notice.setContent(updatedNotice.getContent());
        notice.setPopup(updatedNotice.isPopup());
        notice.setPopupStartDate(updatedNotice.getPopupStartDate());
        notice.setPopupEndDate(updatedNotice.getPopupEndDate());
        log.info("공지사항 수정: {}", notice.getTitle());
        return noticeRepository.save(notice);
    }

    /**
     * 공지사항 삭제.
     *
     * @throws EntityNotFoundException 해당 id의 공지사항이 존재하지 않으면 발생
     */
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
        log.info("공지사항 삭제: {}", notice.getTitle());
        noticeRepository.delete(notice);
    }

    /**
     * 이전글/다음글 조회 — id 기준으로 인접한 공지사항 1건을 가져온다.
     * 목록이 비어있으면 null을 반환하여 템플릿에서 이전/다음 버튼을 숨기도록 처리한다.
     */
    @Transactional(readOnly = true)
    public Notice getPrevNotice(Long id) {
        List<Notice> list = noticeRepository.findPrevNotice(id, PageRequest.of(0, 1));
        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional(readOnly = true)
    public Notice getNextNotice(Long id) {
        List<Notice> list = noticeRepository.findNextNotice(id, PageRequest.of(0, 1));
        return list.isEmpty() ? null : list.get(0);
    }
}
