package com.church.website.service;

import com.church.website.entity.Notice;
import com.church.website.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 모든 공지사항 조회 (최신순)
     */
    public List<Notice> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 페이지네이션 공지사항 조회
     */
    public Page<Notice> getNoticesPaged(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    /**
     * 메인화면용 최근 공지사항 5개 조회
     */
    public List<Notice> getRecentNotices() {
        return noticeRepository.findTop5ByOrderByCreatedAtDesc();
    }

    /**
     * 현재 활성화된 팝업 공지사항 조회
     */
    public List<Notice> getActivePopups() {
        return noticeRepository.findActivePopups(LocalDateTime.now());
    }

    /**
     * 공지사항 상세 조회 및 조회수 증가
     */
    @Transactional
    public Notice getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        notice.setViewCount(notice.getViewCount() + 1);
        return noticeRepository.save(notice);
    }

    /**
     * 공지사항 등록
     */
    @Transactional
    public Notice createNotice(Notice notice) {
        log.info("공지사항 등록: {}", notice.getTitle());
        return noticeRepository.save(notice);
    }

    /**
     * 공지사항 수정
     */
    @Transactional
    public Notice updateNotice(Long id, Notice updatedNotice) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        notice.setTitle(updatedNotice.getTitle());
        notice.setContent(updatedNotice.getContent());
        notice.setPopup(updatedNotice.isPopup());
        notice.setPopupStartDate(updatedNotice.getPopupStartDate());
        notice.setPopupEndDate(updatedNotice.getPopupEndDate());

        log.info("공지사항 수정: {}", notice.getTitle());
        return noticeRepository.save(notice);
    }

    /**
     * 공지사항 삭제
     */
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        log.info("공지사항 삭제: {}", notice.getTitle());
        noticeRepository.delete(notice);
    }
}
