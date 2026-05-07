package com.church.website.service;

import com.church.website.entity.User;
import com.church.website.exception.EntityNotFoundException;
import com.church.website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 관리자 계정 비즈니스 로직 서비스.
 *
 * <p>관리자 계정의 생성·조회·비밀번호 변경·활성화 토글·삭제를 담당한다.
 * 모든 비밀번호는 BCryptPasswordEncoder를 통해 해시한 뒤 저장한다.
 *
 * <p>비밀번호 규칙 (PASSWORD_PATTERN):
 * <ul>
 *   <li>8자 이상</li>
 *   <li>대문자(A-Z), 소문자(a-z), 숫자(0-9)를 각각 하나 이상 포함</li>
 * </ul>
 * 규칙 위반 시 IllegalArgumentException을 던지며,
 * BaseController.run()이 이를 잡아 사용자에게 메시지를 표시한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    // 비밀번호 규칙: 8자 이상, 대문자·소문자·숫자 각 1개 이상 필수
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // BCryptPasswordEncoder (SecurityConfig에서 Bean 등록)

    /** 전체 관리자 계정 목록 조회. */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * ID로 계정 단건 조회.
     *
     * @throws EntityNotFoundException 해당 id의 계정이 존재하지 않으면 발생
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("계정을 찾을 수 없습니다."));
    }

    /**
     * 사용자명으로 계정 단건 조회.
     *
     * @throws EntityNotFoundException 해당 username의 계정이 존재하지 않으면 발생
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("계정을 찾을 수 없습니다."));
    }

    /**
     * 비밀번호 형식 유효성 검사.
     *
     * @throws IllegalArgumentException 규칙에 맞지 않는 비밀번호일 때 발생
     */
    private void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며, 대문자·소문자·숫자를 각각 하나 이상 포함해야 합니다.");
        }
    }

    /**
     * 비밀번호 변경.
     * 현재 비밀번호 일치 여부를 확인한 뒤 새 비밀번호를 BCrypt 해시로 저장한다.
     *
     * @throws IllegalArgumentException 현재 비밀번호 불일치 또는 새 비밀번호가 규칙 미충족 시 발생
     */
    @Transactional
    public void changePassword(String username, String currentPw, String newPw) {
        validatePassword(newPw);
        User user = getUserByUsername(username);
        // BCrypt는 같은 평문도 매번 다른 해시를 만드므로, matches()로만 비교할 수 있다
        if (!passwordEncoder.matches(currentPw, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(newPw));
        userRepository.save(user);
        log.info("비밀번호 변경 완료: {}", username);
    }

    /**
     * 신규 계정 추가.
     * 사용자명 중복 여부와 비밀번호 규칙을 먼저 검증한다.
     * 모든 계정은 ROLE_ADMIN 으로 생성된다.
     *
     * @throws IllegalArgumentException 이미 존재하는 username이거나 비밀번호 규칙 미충족 시 발생
     */
    @Transactional
    public void createUser(String username, String password) {
        validatePassword(password);
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 평문 저장 금지 — 반드시 해시 후 저장
        user.setRole("ROLE_ADMIN");
        user.setEnabled(true);
        userRepository.save(user);
        log.info("계정 추가: {}", username);
    }

    /**
     * 계정 활성/비활성 토글.
     * enabled=true → false, false → true로 전환한다.
     * 현재 로그인 중인 계정을 비활성화하는 건 AdminController에서 사전 차단한다.
     */
    @Transactional
    public void toggleEnabled(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        log.info("계정 상태 변경: {} → enabled={}", user.getUsername(), user.isEnabled());
    }

    /**
     * 계정 삭제.
     * 현재 로그인 중인 계정 삭제는 AdminController에서 사전 차단한다.
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        log.info("계정 삭제: {}", user.getUsername());
    }
}
