package com.church.website.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MS = TimeUnit.MINUTES.toMillis(15);

    private final ConcurrentHashMap<String, LoginRecord> cache = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        cache.remove(username);
    }

    public void loginFailed(String username) {
        LoginRecord record = cache.merge(username,
            new LoginRecord(1, System.currentTimeMillis()),
            (existing, added) -> new LoginRecord(existing.attempts + 1, existing.firstFailedAt)
        );
        if (record.attempts >= MAX_ATTEMPTS) {
            log.warn("로그인 차단: {} ({}회 연속 실패)", username, record.attempts);
        }
    }

    public boolean isBlocked(String username) {
        LoginRecord record = cache.get(username);
        if (record == null || record.attempts < MAX_ATTEMPTS) return false;
        if (System.currentTimeMillis() - record.firstFailedAt > BLOCK_DURATION_MS) {
            cache.remove(username);
            return false;
        }
        return true;
    }

    private record LoginRecord(int attempts, long firstFailedAt) {}
}
