package com.church.website.service;

import com.church.website.entity.Sermon;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.SermonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 설교 영상 비즈니스 로직 서비스.
 *
 * <p>설교 영상의 CRUD와 검색을 담당한다.
 * 영상 파일 자체는 YouTube에 있고, 이 서비스는 YouTube URL·제목·본문 등 메타데이터만 관리한다.
 *
 * <p>수정 시 videoUrl 처리:
 * URL이 입력되지 않으면 기존 URL을 그대로 유지한다.
 * (수정 폼에서 URL을 굳이 다시 입력하지 않아도 됨)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SermonService {

    private final SermonRepository sermonRepository;

    /**
     * 성경 본문 검색 + 페이지네이션으로 설교 목록을 반환한다.
     * biblePassage가 비어있으면 전체를 설교 날짜 내림차순으로 반환한다.
     */
    @Transactional(readOnly = true)
    public Page<Sermon> getSermons(String biblePassage, Pageable pageable) {
        return sermonRepository.searchSermons(biblePassage, pageable);
    }

    /** 메인 페이지용 최신 설교 5건을 반환한다. */
    @Transactional(readOnly = true)
    public List<Sermon> getRecentSermons() {
        return sermonRepository.findTop5ByOrderBySermonDateDesc();
    }

    /**
     * ID로 단건 조회.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional(readOnly = true)
    public Sermon getById(Long id) {
        return sermonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("설교를 찾을 수 없습니다."));
    }

    /** 전체 설교 목록. */
    @Transactional(readOnly = true)
    public List<Sermon> getAll() {
        return sermonRepository.findAll();
    }

    /** 설교 신규 등록. */
    @Transactional
    public Sermon create(Sermon sermon) {
        log.info("설교 등록: {}", sermon.getTitle());
        return sermonRepository.save(sermon);
    }

    /**
     * 설교 정보 수정.
     * videoUrl이 비어있으면 기존 URL을 유지한다.
     * thumbnailUrl이 비어있으면 null로 설정하여 YouTube 기본 썸네일을 사용하도록 한다.
     */
    @Transactional
    public Sermon update(Long id, Sermon updated) {
        Sermon sermon = getById(id);
        sermon.setTitle(updated.getTitle());
        sermon.setPreacher(updated.getPreacher());
        sermon.setSermonDate(updated.getSermonDate());
        sermon.setBiblePassage(updated.getBiblePassage());
        sermon.setDescription(updated.getDescription());
        // URL이 입력된 경우에만 교체 — 비워두면 기존 URL 유지
        if (updated.getVideoUrl() != null && !updated.getVideoUrl().isBlank()) {
            sermon.setVideoUrl(updated.getVideoUrl());
        }
        sermon.setThumbnailUrl(
            (updated.getThumbnailUrl() != null && !updated.getThumbnailUrl().isBlank())
                ? updated.getThumbnailUrl() : null
        );
        log.info("설교 수정: {}", sermon.getTitle());
        return sermonRepository.save(sermon);
    }

    /**
     * 설교 삭제.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional
    public void delete(Long id) {
        Sermon sermon = getById(id);
        log.info("설교 삭제: {}", sermon.getTitle());
        sermonRepository.delete(sermon);
    }
}
