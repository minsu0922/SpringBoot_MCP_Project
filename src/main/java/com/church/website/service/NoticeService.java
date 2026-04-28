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

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public List<Notice> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 관리자 검색 — 키워드/팝업 상태 조건을 QueryDSL로 조합해 DB에서 바로 필터링 */
    @Transactional(readOnly = true)
    public List<Notice> searchNotices(String keyword, String popupStatus) {
        return noticeRepository.searchNotices(keyword, popupStatus);
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return noticeRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<Notice> getNoticesPaged(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Notice> getRecentNotices() {
        return noticeRepository.findTop3ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Notice> getTop5RecentNotices() {
        return noticeRepository.findTop5ByOrderByCreatedAtDesc();
    }

    /**
     * 현재 시각 기준으로 팝업 표시 기간(popupStartDate ~ popupEndDate) 안에 있는 공지사항 조회.
     * 메인 페이지 진입 시 팝업을 띄우는 데 사용.
     */
    @Transactional(readOnly = true)
    public List<Notice> getActivePopups() {
        return noticeRepository.findActivePopups(LocalDateTime.now());
    }

    /**
     * 공지사항 상세 조회 (일반 사용자용).
     * @Modifying UPDATE 한 번으로 조회수 증가 — SELECT+UPDATE 두 번 대신 UPDATE+SELECT 로 처리.
     */
    @Transactional
    public Notice getNoticeById(Long id) {
        noticeRepository.incrementViewCount(id);
        return noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
    }

    /** 공지사항 조회 (관리자용) - 조회수 증가 없음 */
    @Transactional(readOnly = true)
    public Notice getNoticeByIdNoCount(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
    }

    @Transactional
    public Notice createNotice(Notice notice) {
        log.info("공지사항 등록: {}", notice.getTitle());
        return noticeRepository.save(notice);
    }

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

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
        log.info("공지사항 삭제: {}", notice.getTitle());
        noticeRepository.delete(notice);
    }

    /**
     * 이전/다음 글 조회: id 기준으로 인접한 공지사항 1건을 가져옴.
     * 목록이 비어있으면 null을 반환해 템플릿에서 이전/다음 버튼을 숨기도록 처리.
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
