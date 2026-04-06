package com.church.website.service;

import com.church.website.entity.MinistryPhoto;
import com.church.website.repository.MinistryPhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사역 소개 사진 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinistryPhotoService {

    private final MinistryPhotoRepository ministryPhotoRepository;

    /** 활성화된 사진 전체 조회 */
    public List<MinistryPhoto> getActivePhotos() {
        return ministryPhotoRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /** 관리자용 전체 조회 */
    public List<MinistryPhoto> getAllPhotos() {
        return ministryPhotoRepository.findAllByOrderByDisplayOrderAsc();
    }

    /** 단건 조회 */
    public MinistryPhoto getPhotoById(Long id) {
        return ministryPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사역 사진을 찾을 수 없습니다."));
    }

    /** 등록 */
    @Transactional
    public MinistryPhoto createPhoto(MinistryPhoto photo) {
        log.info("사역 사진 등록: {}", photo.getTitle());
        return ministryPhotoRepository.save(photo);
    }

    /** 수정 */
    @Transactional
    public MinistryPhoto updatePhoto(Long id, MinistryPhoto updated) {
        MinistryPhoto photo = ministryPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사역 사진을 찾을 수 없습니다."));
        photo.setCategory(updated.getCategory());
        photo.setTitle(updated.getTitle());
        photo.setDescription(updated.getDescription());
        photo.setPhotoUrl(updated.getPhotoUrl());
        photo.setDisplayOrder(updated.getDisplayOrder());
        photo.setIsActive(updated.getIsActive());
        log.info("사역 사진 수정: {}", photo.getTitle());
        return ministryPhotoRepository.save(photo);
    }

    /** 삭제 */
    @Transactional
    public void deletePhoto(Long id) {
        MinistryPhoto photo = ministryPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사역 사진을 찾을 수 없습니다."));
        log.info("사역 사진 삭제: {}", photo.getTitle());
        ministryPhotoRepository.delete(photo);
    }
}
