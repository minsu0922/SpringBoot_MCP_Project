package com.church.website.service;

import com.church.website.entity.MainImage;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.MainImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainImageService {

    private final MainImageRepository mainImageRepository;

    @Transactional(readOnly = true)
    public List<MainImage> getAllImages() {
        return mainImageRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<MainImage> getActiveImages() {
        return mainImageRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public MainImage getImageById(Long id) {
        return mainImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이미지를 찾을 수 없습니다."));
    }

    @Transactional
    public MainImage createImage(MainImage image) {
        log.info("메인 이미지 등록: {}", image.getTitle());
        return mainImageRepository.save(image);
    }

    @Transactional
    public MainImage updateImage(Long id, MainImage updatedImage) {
        MainImage image = mainImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이미지를 찾을 수 없습니다."));
        image.setTitle(updatedImage.getTitle());
        image.setDescription(updatedImage.getDescription());
        image.setImageUrl(updatedImage.getImageUrl());
        image.setDisplayOrder(updatedImage.getDisplayOrder());
        image.setIsActive(updatedImage.getIsActive());
        log.info("메인 이미지 수정: {}", image.getTitle());
        return mainImageRepository.save(image);
    }

    @Transactional
    public void deleteImage(Long id) {
        MainImage image = mainImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이미지를 찾을 수 없습니다."));
        log.info("메인 이미지 삭제: {}", image.getTitle());
        mainImageRepository.delete(image);
    }
}
