package com.church.website.service;

import com.church.website.entity.User;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    // ──────────────────────────────────────────────
    // 비밀번호 유효성 검증
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("비밀번호 유효성 검증")
    class PasswordValidation {

        @Test
        @DisplayName("대문자·소문자·숫자를 포함한 8자 이상 비밀번호는 통과한다")
        void validPassword() {
            given(userRepository.findByUsername(any())).willReturn(Optional.empty());
            given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            assertThatCode(() -> userService.createUser("user", "Password1"))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest(name = "[{index}] \"{0}\" 은 거부된다")
        @ValueSource(strings = {
                "short1A",        // 7자 (너무 짧음)
                "nouppercase1",   // 대문자 없음
                "NOLOWERCASE1",   // 소문자 없음
                "NoDigitHere",    // 숫자 없음
                ""                // 빈 문자열
        })
        @DisplayName("규칙에 맞지 않는 비밀번호는 예외를 던진다")
        void invalidPassword(String password) {
            assertThatThrownBy(() -> userService.createUser("user", password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호");
        }
    }

    // ──────────────────────────────────────────────
    // 계정 생성
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("계정 생성")
    class CreateUser {

        @Test
        @DisplayName("중복 사용자명이면 예외를 던진다")
        void duplicateUsernameThrows() {
            given(userRepository.findByUsername("admin"))
                    .willReturn(Optional.of(new User()));

            assertThatThrownBy(() -> userService.createUser("admin", "Password1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 존재하는");
        }

        @Test
        @DisplayName("저장된 비밀번호는 BCrypt로 암호화된다")
        void passwordIsEncoded() {
            given(userRepository.findByUsername(any())).willReturn(Optional.empty());
            given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            userService.createUser("newUser", "Password1");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            String savedPassword = captor.getValue().getPassword();
            assertThat(passwordEncoder.matches("Password1", savedPassword)).isTrue();
        }

        @Test
        @DisplayName("생성된 계정의 역할은 ROLE_ADMIN이다")
        void roleIsAdmin() {
            given(userRepository.findByUsername(any())).willReturn(Optional.empty());
            given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            userService.createUser("newUser", "Password1");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().getRole()).isEqualTo("ROLE_ADMIN");
            assertThat(captor.getValue().isEnabled()).isTrue();
        }
    }

    // ──────────────────────────────────────────────
    // 비밀번호 변경
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("현재 비밀번호가 틀리면 예외를 던진다")
        void wrongCurrentPasswordThrows() {
            User user = new User();
            user.setPassword(passwordEncoder.encode("OldPass1"));
            given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));

            assertThatThrownBy(() ->
                    userService.changePassword("admin", "WrongPass1", "NewPass1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("현재 비밀번호");
        }

        @Test
        @DisplayName("올바른 현재 비밀번호면 새 비밀번호로 변경된다")
        void successfulPasswordChange() {
            User user = new User();
            user.setPassword(passwordEncoder.encode("OldPass1"));
            given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));
            given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            userService.changePassword("admin", "OldPass1", "NewPass1");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            assertThat(passwordEncoder.matches("NewPass1", captor.getValue().getPassword())).isTrue();
        }

        @Test
        @DisplayName("새 비밀번호가 규칙에 맞지 않으면 예외를 던진다")
        void invalidNewPasswordThrows() {
            // changePassword는 새 비밀번호 유효성 검사를 먼저 수행하므로 DB 조회 전에 예외 발생
            assertThatThrownBy(() ->
                    userService.changePassword("admin", "OldPass1", "weak"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호");
        }
    }

    // ──────────────────────────────────────────────
    // 계정 조회
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("계정 조회")
    class GetUser {

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 EntityNotFoundException")
        void notFoundById() {
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 사용자명 조회 시 EntityNotFoundException")
        void notFoundByUsername() {
            given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByUsername("unknown"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
