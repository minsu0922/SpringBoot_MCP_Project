package com.church.website.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 로그인 실패 횟수를 추적하여 브루트포스 공격을 방어하는 서비스.
 *
 * <p>동작 방식:
 * <ul>
 *   <li>사용자별 로그인 실패 횟수를 인메모리 캐시(ConcurrentHashMap)에 기록한다.</li>
 *   <li>실패 횟수가 MAX_ATTEMPTS(5회)에 도달하면 해당 계정을 잠근다.</li>
 *   <li>첫 실패 시각 기준으로 BLOCK_DURATION_MS(15분)가 지나면 자동으로 잠금이 해제된다.</li>
 *   <li>로그인 성공 시 해당 계정의 실패 기록을 즉시 초기화한다.</li>
 * </ul>
 *
 * <p>주의: 캐시는 애플리케이션 재시작 시 초기화된다(영속화하지 않음).
 * 분산 환경에서는 서버 인스턴스마다 캐시가 독립적으로 존재하므로 Redis 등으로 교체 필요.
 */
@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MS = TimeUnit.MINUTES.toMillis(15);

    // 사용자명 → 실패 기록 매핑. 멀티스레드 환경에서 안전하게 동시 접근을 처리한다.
    private final ConcurrentHashMap<String, LoginRecord> cache = new ConcurrentHashMap<>();

    /** 로그인 성공 시 호출. 해당 사용자의 실패 기록을 삭제하여 잠금 상태를 해제한다. */
    public void loginSucceeded(String username) {
        cache.remove(username);
    }

    /**
     * 로그인 실패 시 호출. 실패 횟수를 1 증가시키고, MAX_ATTEMPTS 도달 시 경고 로그를 남긴다.
     * 첫 실패라면 새 레코드를 생성하고, 이미 기록이 있으면 firstFailedAt은 유지한 채 횟수만 누적한다.
     */
    public void loginFailed(String username) {
        LoginRecord record = cache.merge(username,
            new LoginRecord(1, System.currentTimeMillis()),
            (existing, added) -> new LoginRecord(existing.attempts + 1, existing.firstFailedAt)
        );
        if (record.attempts >= MAX_ATTEMPTS) {
            log.warn("로그인 차단: {} ({}회 연속 실패)", username, record.attempts);
        }
    }

    /**
     * 해당 사용자가 현재 차단 상태인지 확인한다.
     *
     * @return 실패 횟수가 MAX_ATTEMPTS 이상이고 차단 유지 시간이 남아 있으면 true.
     *         차단 시간이 경과했으면 캐시에서 제거 후 false 반환.
     */
    public boolean isBlocked(String username) {
        LoginRecord record = cache.get(username);
        if (record == null || record.attempts < MAX_ATTEMPTS) return false;
        if (System.currentTimeMillis() - record.firstFailedAt > BLOCK_DURATION_MS) {
            cache.remove(username);
            return false;
        }
        return true;
    }

    /** 사용자별 실패 기록. attempts: 누적 실패 횟수, firstFailedAt: 첫 실패 시각(ms). */
    private record LoginRecord(int attempts, long firstFailedAt) {}
}
