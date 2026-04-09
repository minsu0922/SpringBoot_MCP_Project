package com.church.website.service;

import com.church.website.entity.User;
import com.church.website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자 계정 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 전체 계정 목록 조회 */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** 단건 조회 */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("계정을 찾을 수 없습니다."));
    }

    /** 사용자명으로 조회 */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("계정을 찾을 수 없습니다."));
    }

    /**
     * 비밀번호 변경
     * @param username      현재 로그인된 사용자명
     * @param currentPw     현재 비밀번호 (검증용)
     * @param newPw         새 비밀번호
     */
    @Transactional
    public void changePassword(String username, String currentPw, String newPw) {
        User user = getUserByUsername(username);
        if (!passwordEncoder.matches(currentPw, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(newPw));
        userRepository.save(user);
        log.info("비밀번호 변경 완료: {}", username);
    }

    /**
     * 계정 추가
     */
    @Transactional
    public void createUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_ADMIN");
        user.setEnabled(true);
        userRepository.save(user);
        log.info("계정 추가: {}", username);
    }

    /**
     * 계정 활성/비활성 토글
     */
    @Transactional
    public void toggleEnabled(Long id) {
        User user = getUserById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        log.info("계정 상태 변경: {} → enabled={}", user.getUsername(), user.isEnabled());
    }

    /**
     * 계정 삭제
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        log.info("계정 삭제: {}", user.getUsername());
    }
}
