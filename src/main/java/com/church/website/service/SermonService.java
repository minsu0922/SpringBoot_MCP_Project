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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SermonService {

    private final SermonRepository sermonRepository;

    @Value("${file.upload.sermon.dir}")
    private String sermonUploadDir;

    public String saveFile(MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path dir = Paths.get(sermonUploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : "";
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
        if (biblePassage != null && !biblePassage.isBlank()) {
            return sermonRepository.findByBiblePassageContainingIgnoreCase(biblePassage, pageable);
        }
        return sermonRepository.findAll(pageable);
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
        if (newVideoUrl != null && !newVideoUrl.isBlank()) {
            deleteFile(sermon.getVideoUrl());
            sermon.setVideoUrl(newVideoUrl);
        }
        if (newThumbnailUrl != null && !newThumbnailUrl.isBlank()) {
            deleteFile(sermon.getThumbnailUrl());
            sermon.setThumbnailUrl(newThumbnailUrl);
        }
        log.info("설교 수정: {}", sermon.getTitle());
        return sermonRepository.save(sermon);
    }

    @Transactional
    public void delete(Long id) {
        Sermon sermon = getById(id);
        deleteFile(sermon.getVideoUrl());
        deleteFile(sermon.getThumbnailUrl());
        log.info("설교 삭제: {}", sermon.getTitle());
        sermonRepository.delete(sermon);
    }
}
