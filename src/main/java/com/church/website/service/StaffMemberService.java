package com.church.website.service;

import com.church.website.entity.StaffMember;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.StaffMemberRepository;
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
public class StaffMemberService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_MIME_TYPES  = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final StaffMemberRepository staffMemberRepository;

    @Value("${file.upload.people.dir}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<StaffMember> getActive() {
        return staffMemberRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<StaffMember> getAll() {
        return staffMemberRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public StaffMember getById(Long id) {
        return staffMemberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("교역자를 찾을 수 없습니다."));
    }

    @Transactional
    public StaffMember create(StaffMember member) {
        List<StaffMember> all = staffMemberRepository.findAllByOrderByDisplayOrderAsc();
        member.setDisplayOrder(all.isEmpty() ? 0 : all.size());
        return staffMemberRepository.save(member);
    }

    @Transactional
    public StaffMember update(Long id, StaffMember updated, String newPhotoUrl) {
        StaffMember member = getById(id);
        member.setName(updated.getName());
        member.setRole(updated.getRole());
        member.setMinistry(updated.getMinistry());
        member.setBio(updated.getBio());
        member.setIsActive(updated.getIsActive());

        String oldPhotoUrl = null;
        if (newPhotoUrl != null && !newPhotoUrl.isBlank()) {
            oldPhotoUrl = member.getPhotoUrl();
            member.setPhotoUrl(newPhotoUrl);
        }

        StaffMember saved = staffMemberRepository.save(member);
        deletePhoto(oldPhotoUrl);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        StaffMember member = getById(id);
        String photoUrl = member.getPhotoUrl();
        staffMemberRepository.delete(member);
        deletePhoto(photoUrl);
    }

    @Transactional
    public void moveUp(Long id) {
        List<StaffMember> all = staffMemberRepository.findAllByOrderByDisplayOrderAsc();
        normalize(all);
        int idx = indexOf(all, id);
        if (idx <= 0) return;
        all.get(idx).setDisplayOrder(idx - 1);
        all.get(idx - 1).setDisplayOrder(idx);
        staffMemberRepository.saveAll(all);
    }

    @Transactional
    public void moveDown(Long id) {
        List<StaffMember> all = staffMemberRepository.findAllByOrderByDisplayOrderAsc();
        normalize(all);
        int idx = indexOf(all, id);
        if (idx < 0 || idx >= all.size() - 1) return;
        all.get(idx).setDisplayOrder(idx + 1);
        all.get(idx + 1).setDisplayOrder(idx);
        staffMemberRepository.saveAll(all);
    }

    private void normalize(List<StaffMember> list) {
        for (int i = 0; i < list.size(); i++) list.get(i).setDisplayOrder(i);
    }

    private int indexOf(List<StaffMember> list, Long id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) return i;
        }
        return -1;
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

        log.info("교역자 사진 저장: {}", dest);
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
            log.info("교역자 사진 삭제: {}", target);
        } catch (IOException e) {
            log.warn("교역자 사진 삭제 실패: {}", photoUrl, e);
        }
    }
}
