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

/**
 * 사역 소개 사진 비즈니스 로직 서비스.
 *
 * <p>사역 사진의 파일 저장/삭제와 DB CRUD를 담당한다.
 * 실제 이미지 파일은 {@code file.upload.dir} 디렉토리에 저장하고,
 * DB에는 웹에서 접근 가능한 URL 경로만 저장한다.
 *
 * <p>파일 보안 처리:
 * <ul>
 *   <li>MIME 타입과 확장자를 둘 다 검증하여 위장된 파일 업로드를 차단한다.</li>
 *   <li>저장 파일명은 UUID로 무작위 생성하여 원본 파일명 노출과 중복을 방지한다.</li>
 *   <li>파일 삭제 시 Path Traversal 공격을 방어하기 위해 경로를 정규화(normalize)한 뒤
 *       업로드 디렉토리 내부인지 확인한다.</li>
 * </ul>
 *
 * <p>수정 시 파일 교체 순서: DB 저장 성공 → 구 파일 삭제.
 * DB 저장 실패 시 새 파일이 남아있는 편이 구 파일이 사라지는 것보다 낫기 때문이다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinistryPhotoService {

    // 허용 확장자 목록 — 소문자로 비교
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    // 허용 MIME 타입 목록 — 확장자만으로는 위장 가능하므로 MIME도 함께 검증
    private static final Set<String> ALLOWED_MIME_TYPES  = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final MinistryPhotoRepository ministryPhotoRepository;

    // application.properties의 file.upload.dir 값 (사역 사진 저장 디렉토리)
    @Value("${file.upload.dir}")
    private String uploadDir;

    /**
     * 사역 사진 파일을 서버에 저장하고 접근 URL을 반환한다.
     *
     * @param file 업로드된 멀티파트 파일
     * @return "/uploads/ministry/파일명.확장자" 형태의 URL. null 또는 빈 파일이면 null 반환.
     * @throws IllegalArgumentException 허용되지 않는 MIME 타입 또는 확장자일 때
     * @throws IOException              파일 저장 중 IO 오류 발생 시
     */
    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // MIME 타입 검증 — 확장자를 jpg로 바꾼 실행 파일 등 위장 차단
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (허용: jpg, jpeg, png, gif, webp)");
        }

        // 확장자 추출 및 검증
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다.");
        }

        // 업로드 디렉토리가 없으면 자동 생성
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // UUID로 무작위 파일명 생성 — 원본 파일명 노출 방지 및 중복 파일명 충돌 예방
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = dir.resolve(savedName);
        file.transferTo(dest.toFile());

        log.info("파일 저장 완료: {}", dest);
        return "/uploads/ministry/" + savedName;
    }

    /**
     * 사역 사진 파일을 서버에서 삭제한다.
     *
     * <p>/uploads/ministry/ 로 시작하지 않는 경로는 무시하여
     * 다른 디렉토리의 파일이 삭제되는 사고를 방지한다.
     * Path Traversal 방어: 경로를 정규화(normalize)한 뒤 업로드 루트 내부인지 검증한다.
     */
    public void deleteFile(String photoUrl) {
        if (photoUrl == null || !photoUrl.startsWith("/uploads/ministry/")) return;
        try {
            String filename = photoUrl.replace("/uploads/ministry/", "");
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target    = uploadRoot.resolve(filename).normalize();
            // "../" 같은 경로 탈출 시도를 정규화 후 startsWith로 차단
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

    /** 활성화된 사진만 표시 순서대로 반환한다. 공개 페이지에서 사용한다. */
    @Transactional(readOnly = true)
    public List<MinistryPhoto> getActivePhotos() {
        return ministryPhotoRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /** 전체 사진을 표시 순서대로 반환한다. 관리자 목록에서 사용한다. */
    @Transactional(readOnly = true)
    public List<MinistryPhoto> getAllPhotos() {
        return ministryPhotoRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * ID로 단건 조회.
     *
     * @throws EntityNotFoundException 해당 id가 존재하지 않으면 발생
     */
    @Transactional(readOnly = true)
    public MinistryPhoto getPhotoById(Long id) {
        return ministryPhotoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사역 사진을 찾을 수 없습니다."));
    }

    /** 사역 사진 신규 등록. */
    @Transactional
    public MinistryPhoto createPhoto(MinistryPhoto photo) {
        log.info("사역 사진 등록: {}", photo.getTitle());
        return ministryPhotoRepository.save(photo);
    }

    /**
     * 사역 사진 수정.
     * 새 파일이 업로드된 경우에만 기존 파일을 교체한다.
     * DB 저장 성공 후 구 파일을 삭제하여 저장 실패 시 파일 손실을 방지한다.
     */
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

    /**
     * 사역 사진 DB 레코드 삭제.
     * 파일 삭제는 호출한 컨트롤러(AdminController)에서 별도로 처리한다.
     */
    @Transactional
    public void deletePhoto(Long id) {
        MinistryPhoto photo = ministryPhotoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사역 사진을 찾을 수 없습니다."));
        log.info("사역 사진 삭제: {}", photo.getTitle());
        ministryPhotoRepository.delete(photo);
    }

    /** 전체 사역 사진 건수. 관리자 대시보드 통계에 사용한다. */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return ministryPhotoRepository.count();
    }
}
