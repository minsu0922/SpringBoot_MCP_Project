package com.church.website.service;

import com.church.website.entity.Pastor;
import com.church.website.repository.PastorRepository;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PastorService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_MIME_TYPES  = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final PastorRepository pastorRepository;

    @Value("${file.upload.people.dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public Optional<Pastor> getActive() {
        return pastorRepository.findFirstByIsActiveTrueOrderByIdDesc();
    }

    @Transactional
    public Pastor save(Pastor updated, String newPhotoUrl) {
        Pastor pastor = pastorRepository.findFirstByIsActiveTrueOrderByIdDesc()
                .orElse(new Pastor());
        if (updated.getName() != null && !updated.getName().isBlank())
            pastor.setName(updated.getName());
        if (updated.getRole() != null && !updated.getRole().isBlank())
            pastor.setRole(updated.getRole());
        pastor.setGreeting(updated.getGreeting());
        pastor.setIsActive(true);

        String oldPhotoUrl = null;
        if (newPhotoUrl != null && !newPhotoUrl.isBlank()) {
            oldPhotoUrl = pastor.getPhotoUrl();
            pastor.setPhotoUrl(newPhotoUrl);
        }

        Pastor saved = pastorRepository.save(pastor);
        deletePhoto(oldPhotoUrl);
        return saved;
    }

    public String savePhoto(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (허용: jpg, jpeg, png, gif, webp)");
        }

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase() : "";
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

        log.info("담임목사 사진 저장: {}", dest);
        return "/uploads/people/" + savedName;
    }

    public void deletePhoto(String photoUrl) {
        if (photoUrl == null || !photoUrl.startsWith("/uploads/people/")) return;
        try {
            String filename = photoUrl.replace("/uploads/people/", "");
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target     = uploadRoot.resolve(filename).normalize();
            if (!target.startsWith(uploadRoot)) {
                log.warn("허용되지 않는 파일 경로 접근 차단: {}", photoUrl);
                return;
            }
            Files.deleteIfExists(target);
            log.info("담임목사 사진 삭제: {}", target);
        } catch (IOException e) {
            log.warn("담임목사 사진 삭제 실패: {}", photoUrl, e);
        }
    }
}
