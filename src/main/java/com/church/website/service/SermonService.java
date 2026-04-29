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

@Slf4j
@Service
@RequiredArgsConstructor
public class SermonService {

    private final SermonRepository sermonRepository;

    @Transactional(readOnly = true)
    public Page<Sermon> getSermons(String biblePassage, Pageable pageable) {
        return sermonRepository.searchSermons(biblePassage, pageable);
    }

    @Transactional(readOnly = true)
    public List<Sermon> getRecentSermons() {
        return sermonRepository.findTop5ByOrderBySermonDateDesc();
    }

    @Transactional(readOnly = true)
    public Sermon getById(Long id) {
        return sermonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("설교를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Sermon> getAll() {
        return sermonRepository.findAll();
    }

    @Transactional
    public Sermon create(Sermon sermon) {
        log.info("설교 등록: {}", sermon.getTitle());
        return sermonRepository.save(sermon);
    }

    @Transactional
    public Sermon update(Long id, Sermon updated) {
        Sermon sermon = getById(id);
        sermon.setTitle(updated.getTitle());
        sermon.setPreacher(updated.getPreacher());
        sermon.setSermonDate(updated.getSermonDate());
        sermon.setBiblePassage(updated.getBiblePassage());
        sermon.setDescription(updated.getDescription());
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

    @Transactional
    public void delete(Long id) {
        Sermon sermon = getById(id);
        log.info("설교 삭제: {}", sermon.getTitle());
        sermonRepository.delete(sermon);
    }
}
