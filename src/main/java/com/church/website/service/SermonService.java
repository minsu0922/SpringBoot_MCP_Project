package com.church.website.service;

import com.church.website.entity.Sermon;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.SermonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class SermonService {

    private static final Set<String> ALLOWED_VIDEO_EXT  = Set.of(".mp4", ".webm", ".mov", ".avi", ".mkv");
    private static final Set<String> ALLOWED_VIDEO_MIME = Set.of(
            "video/mp4", "video/webm", "video/quicktime", "video/x-msvideo", "video/x-matroska");
    private static final Set<String> ALLOWED_THUMB_EXT  = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_THUMB_MIME = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private final SermonRepository sermonRepository;

    @Value("${file.upload.sermon.dir}")
    private String sermonUploadDir;

    public String saveFile(MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : "";

        boolean isThumb = "thumb".equals(prefix);
        Set<String> allowedExt  = isThumb ? ALLOWED_THUMB_EXT  : ALLOWED_VIDEO_EXT;
        Set<String> allowedMime = isThumb ? ALLOWED_THUMB_MIME : ALLOWED_VIDEO_MIME;
        String typeLabel = isThumb ? "이미지(jpg·png·gif·webp)" : "동영상(mp4·webm·mov·avi·mkv)";

        if (!allowedExt.contains(ext) || !allowedMime.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. 허용: " + typeLabel);
        }

        Path dir = Paths.get(sermonUploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String savedName = prefix + "-" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = dir.resolve(savedName);
        file.transferTo(dest.toFile());

        log.info("설교 파일 저장: {}", dest);
        return "/uploads/sermon/" + savedName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/sermon/")) return;
        try {
            Path file = Paths.get(sermonUploadDir).resolve(fileUrl.replace("/uploads/sermon/", ""));
            Files.deleteIfExists(file);
            log.info("설교 파일 삭제: {}", file);
        } catch (IOException e) {
            log.warn("설교 파일 삭제 실패: {}", fileUrl, e);
        }
    }

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
    public Sermon update(Long id, Sermon updated, String newVideoUrl, String newThumbnailUrl) {
        Sermon sermon = getById(id);
        sermon.setTitle(updated.getTitle());
        sermon.setPreacher(updated.getPreacher());
        sermon.setSermonDate(updated.getSermonDate());
        sermon.setBiblePassage(updated.getBiblePassage());
        sermon.setDescription(updated.getDescription());

        String oldVideoUrl = null;
        String oldThumbnailUrl = null;
        if (newVideoUrl != null && !newVideoUrl.isBlank()) {
            oldVideoUrl = sermon.getVideoUrl();
            sermon.setVideoUrl(newVideoUrl);
        }
        if (newThumbnailUrl != null && !newThumbnailUrl.isBlank()) {
            oldThumbnailUrl = sermon.getThumbnailUrl();
            sermon.setThumbnailUrl(newThumbnailUrl);
        }
        log.info("설교 수정: {}", sermon.getTitle());
        Sermon saved = sermonRepository.save(sermon);
        // DB 저장 성공 후 이전 파일 삭제 — 순서가 반대면 저장 실패 시 파일만 사라짐
        deleteFile(oldVideoUrl);
        deleteFile(oldThumbnailUrl);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Sermon sermon = getById(id);
        String videoUrl     = sermon.getVideoUrl();
        String thumbnailUrl = sermon.getThumbnailUrl();
        log.info("설교 삭제: {}", sermon.getTitle());
        sermonRepository.delete(sermon);   // DB 먼저 — 실패 시 파일 보존
        deleteFile(videoUrl);
        deleteFile(thumbnailUrl);
    }
}
