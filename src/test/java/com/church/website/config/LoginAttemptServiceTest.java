package com.church.website.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginAttemptService 테스트")
class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Test
    @DisplayName("초기 상태에서는 차단되지 않는다")
    void notBlockedInitially() {
        assertThat(service.isBlocked("user")).isFalse();
    }

    @Test
    @DisplayName("4회 실패해도 차단되지 않는다")
    void notBlockedAfterFourFailures() {
        for (int i = 0; i < 4; i++) {
            service.loginFailed("user");
        }
        assertThat(service.isBlocked("user")).isFalse();
    }

    @Test
    @DisplayName("5회 실패하면 차단된다")
    void blockedAfterFiveFailures() {
        for (int i = 0; i < 5; i++) {
            service.loginFailed("user");
        }
        assertThat(service.isBlocked("user")).isTrue();
    }

    @Test
    @DisplayName("6회 이상 실패해도 차단 상태가 유지된다")
    void remainsBlockedAfterMoreFailures() {
        for (int i = 0; i < 10; i++) {
            service.loginFailed("user");
        }
        assertThat(service.isBlocked("user")).isTrue();
    }

    @Test
    @DisplayName("차단 후 로그인 성공하면 즉시 차단이 해제된다")
    void unblockAfterSuccess() {
        for (int i = 0; i < 5; i++) {
            service.loginFailed("user");
        }
        assertThat(service.isBlocked("user")).isTrue();

        service.loginSucceeded("user");

        assertThat(service.isBlocked("user")).isFalse();
    }

    @Test
    @DisplayName("로그인 성공 후 실패 카운트가 초기화된다")
    void failureCountResetAfterSuccess() {
        for (int i = 0; i < 4; i++) {
            service.loginFailed("user");
        }
        service.loginSucceeded("user");

        // 성공 후 다시 4회 실패 → 차단 안 됨
        for (int i = 0; i < 4; i++) {
            service.loginFailed("user");
        }
        assertThat(service.isBlocked("user")).isFalse();
    }

    @Test
    @DisplayName("사용자 A의 실패가 사용자 B에게 영향을 주지 않는다")
    void independentPerUser() {
        for (int i = 0; i < 5; i++) {
            service.loginFailed("userA");
        }

        assertThat(service.isBlocked("userA")).isTrue();
        assertThat(service.isBlocked("userB")).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 성공 처리는 예외 없이 통과한다")
    void successOnUnknownUserIsHarmless() {
        // 한 번도 실패한 적 없는 사용자에 대한 성공 호출
        service.loginSucceeded("unknownUser");

        assertThat(service.isBlocked("unknownUser")).isFalse();
    }
}
