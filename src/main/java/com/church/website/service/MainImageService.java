package com.church.website.service;

import com.church.website.entity.MainImage;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.MainImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 메인 페이지 슬라이더 이미지 비즈니스 로직 서비스.
 *
 * <p>메인 홈 화면 상단의 이미지 슬라이더에 표시되는 이미지를 관리한다.
 * isActive=true 인 이미지만 공개 화면에 표시하며, displayOrder 오름차순으로 정렬된다.
 *
 * <p>현재 관리자 UI가 없어 DB를 직접 조작해야 한다.
 * 향후 관리자 페이지에서 이미지 업로드·순서 조정을 지원할 때 이 서비스를 활용하면 된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainImageService {

    private final MainImageRepository mainImageRepository;

    /** 전체 이미지를 표시 순서대로 반환한다. (관리자용) */
    @Transactional(readOnly = true)
    public List<MainImage> getAllImages() {
        return mainImageRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * 활성화된 이미지만 표시 순서대로 반환한다.
     * 메인 페이지 슬라이더에 사용한다.
     */
    @Transactional(readOnly = true)
    public List<MainImage> getActiveImages() {
        return mainImageRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * ID로 단건 조회.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional(readOnly = true)
    public MainImage getImageById(Long id) {
        return mainImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이미지를 찾을 수 없습니다."));
    }

    /** 새 이미지 등록. */
    @Transactional
    public MainImage createImage(MainImage image) {
        log.info("메인 이미지 등록: {}", image.getTitle());
        return mainImageRepository.save(image);
    }

    /**
     * 이미지 정보 수정.
     * 엔티티를 먼저 조회한 뒤 필드를 업데이트하는 방식(Dirty Checking)을 사용하여
     * 변경된 필드만 DB에 업데이트된다.
     */
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

    /**
     * 이미지 삭제.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional
    public void deleteImage(Long id) {
        MainImage image = mainImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이미지를 찾을 수 없습니다."));
        log.info("메인 이미지 삭제: {}", image.getTitle());
        mainImageRepository.delete(image);
    }
}
