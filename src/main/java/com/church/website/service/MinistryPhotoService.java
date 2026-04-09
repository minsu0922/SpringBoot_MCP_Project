package com.church.website.service;

import com.church.website.entity.MinistryPhoto;
import com.church.website.repository.MinistryPhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 사역 소개 사진 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinistryPhotoService {

    private final MinistryPhotoRepository ministryPhotoRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    /**
     * 파일 업로드 처리 후 저장 경로(URL) 반환
     */
    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path dest = dir.resolve(savedName);
        file.transferTo(dest.toFile());

        log.info("파일 저장 완료: {}", dest);
        return "/uploads/ministry/" + savedName;
    }

    /**
     * 파일 삭제 (기존 파일 교체 시 사용)
     */
    public void deleteFile(String photoUrl) {
        if (photoUrl == null || !photoUrl.startsWith("/uploads/ministry/")) return;
        try {
            Path file = Paths.get(uploadDir).resolve(photoUrl.replace("/uploads/ministry/", ""));
            Files.deleteIfExists(file);
            log.info("파일 삭제 완료: {}", file);
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", photoUrl, e);
        }
    }

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
    public MinistryPhoto updatePhoto(Long id, MinistryPhoto updated, String newPhotoUrl) {
        MinistryPhoto photo = ministryPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사역 사진을 찾을 수 없습니다."));
        photo.setCategory(updated.getCategory());
        photo.setTitle(updated.getTitle());
        photo.setDescription(updated.getDescription());
        if (newPhotoUrl != null && !newPhotoUrl.isBlank()) {
            deleteFile(photo.getPhotoUrl()); // 기존 파일 삭제
            photo.setPhotoUrl(newPhotoUrl);
        }
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
