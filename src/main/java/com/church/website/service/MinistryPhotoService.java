package com.church.website.service;

import com.church.website.entity.MinistryPhoto;
import com.church.website.exception.EntityNotFoundException;
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
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinistryPhotoService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_MIME_TYPES  = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final MinistryPhotoRepository ministryPhotoRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (허용: jpg, jpeg, png, gif, webp)");
        }

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다.");
        }

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = dir.resolve(savedName);
        file.transferTo(dest.toFile());

        log.info("파일 저장 완료: {}", dest);
        return "/uploads/ministry/" + savedName;
    }

    public void deleteFile(String photoUrl) {
        if (photoUrl == null || !photoUrl.startsWith("/uploads/ministry/")) return;
        try {
            String filename = photoUrl.replace("/uploads/ministry/", "");
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target    = uploadRoot.resolve(filename).normalize();
            // Path Traversal 방지: 정규화 후 업로드 디렉토리 내부인지 확인
            if (!target.startsWith(uploadRoot)) {
                log.warn("허용되지 않는 파일 경로 접근 차단: {}", photoUrl);
                return;
            }
            Files.deleteIfExists(target);
            log.info("파일 삭제 완료: {}", target);
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", photoUrl, e);
        }
    }

    @Transactional(readOnly = true)
    public List<MinistryPhoto> getActivePhotos() {
        return ministryPhotoRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<MinistryPhoto> getAllPhotos() {
        return ministryPhotoRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public MinistryPhoto getPhotoById(Long id) {
        return ministryPhotoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사역 사진을 찾을 수 없습니다."));
    }

    @Transactional
    public MinistryPhoto createPhoto(MinistryPhoto photo) {
        log.info("사역 사진 등록: {}", photo.getTitle());
        return ministryPhotoRepository.save(photo);
    }

    /** 새 파일이 업로드된 경우에만 기존 파일을 삭제하고 교체. DB 저장 성공 후 파일 삭제. */
    @Transactional
    public MinistryPhoto updatePhoto(Long id, MinistryPhoto updated, String newPhotoUrl) {
        MinistryPhoto photo = ministryPhotoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사역 사진을 찾을 수 없습니다."));
        photo.setCategory(updated.getCategory());
        photo.setTitle(updated.getTitle());
        photo.setDescription(updated.getDescription());
        String oldPhotoUrl = null;
        if (newPhotoUrl != null && !newPhotoUrl.isBlank()) {
            oldPhotoUrl = photo.getPhotoUrl();
            photo.setPhotoUrl(newPhotoUrl);
        }
        photo.setDisplayOrder(updated.getDisplayOrder());
        photo.setIsActive(updated.getIsActive());
        log.info("사역 사진 수정: {}", photo.getTitle());
        MinistryPhoto saved = ministryPhotoRepository.save(photo);  // DB 먼저
        deleteFile(oldPhotoUrl);  // 저장 성공 후 구 파일 삭제
        return saved;
    }

    @Transactional
    public void deletePhoto(Long id) {
        MinistryPhoto photo = ministryPhotoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사역 사진을 찾을 수 없습니다."));
        log.info("사역 사진 삭제: {}", photo.getTitle());
        ministryPhotoRepository.delete(photo);
    }

    @Transactional(readOnly = true)
    public long getTotalCount() {
        return ministryPhotoRepository.count();
    }
}
