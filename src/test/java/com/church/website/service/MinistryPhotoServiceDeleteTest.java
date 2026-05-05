package com.church.website.service;

import com.church.website.repository.MinistryPhotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("MinistryPhotoService.deleteFile() 보안 테스트")
class MinistryPhotoServiceDeleteTest {

    @Mock
    private MinistryPhotoRepository ministryPhotoRepository;

    private MinistryPhotoService service;

    @TempDir
    Path tempDir;

    private Path uploadDir;

    @BeforeEach
    void setUp() throws IOException {
        // tempDir/uploads/ 를 업로드 디렉토리로 사용
        uploadDir = tempDir.resolve("uploads");
        Files.createDirectories(uploadDir);

        service = new MinistryPhotoService(ministryPhotoRepository);
        ReflectionTestUtils.setField(service, "uploadDir", uploadDir.toString());
    }

    @Test
    @DisplayName("null URL은 예외 없이 무시한다")
    void ignoresNullUrl() {
        assertThatCode(() -> service.deleteFile(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("/uploads/ministry/ 접두사가 없는 경로는 무시한다")
    void ignoresInvalidPrefix() {
        assertThatCode(() -> service.deleteFile("/uploads/other/secret.jpg"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("정상 경로의 파일을 삭제한다")
    void deletesValidFile() throws IOException {
        Path file = uploadDir.resolve("photo.jpg");
        Files.createFile(file);
        assertThat(file).exists();

        service.deleteFile("/uploads/ministry/photo.jpg");

        assertThat(file).doesNotExist();
    }

    @Test
    @DisplayName("존재하지 않는 파일은 예외 없이 통과한다")
    void silentlyIgnoresMissingFile() {
        assertThatCode(() -> service.deleteFile("/uploads/ministry/nonexistent.jpg"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Path Traversal (../) 시도 시 업로드 디렉토리 밖 파일을 삭제하지 않는다")
    void blocksPathTraversal() throws IOException {
        // uploads/ 밖에 민감한 파일 생성
        Path sensitiveFile = tempDir.resolve("secret.txt");
        Files.writeString(sensitiveFile, "민감한 데이터");

        // ../secret.txt 로 업로드 디렉토리 밖 접근 시도
        service.deleteFile("/uploads/ministry/../secret.txt");

        // 파일이 삭제되지 않고 그대로 존재해야 함
        assertThat(sensitiveFile).exists();
    }

    @Test
    @DisplayName("중첩 ../ 경로 시도도 차단된다")
    void blocksNestedPathTraversal() throws IOException {
        Path sensitiveFile = tempDir.resolve("sensitive.conf");
        Files.writeString(sensitiveFile, "설정 데이터");

        service.deleteFile("/uploads/ministry/../../sensitive.conf");

        assertThat(sensitiveFile).exists();
    }

    @Test
    @DisplayName("절대 경로 삽입 시도는 차단된다")
    void blocksAbsolutePathInjection() throws IOException {
        // 업로드 디렉토리 밖의 임의 파일 생성 후, URL에 절대 경로 삽입 시도
        Path outsideFile = tempDir.resolve("outside.txt");
        Files.writeString(outsideFile, "외부 파일");

        // resolve 시 절대 경로가 삽입되면 uploadRoot 밖으로 벗어남
        service.deleteFile("/uploads/ministry/" + outsideFile.toAbsolutePath());

        assertThat(outsideFile).exists();
    }
}
