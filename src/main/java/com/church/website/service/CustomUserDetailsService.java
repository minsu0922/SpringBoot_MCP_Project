package com.church.website.service;

import com.church.website.entity.User;
import com.church.website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security 사용자 인증 서비스
 * 데이터베이스에서 사용자 정보를 조회하여 인증 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자명으로 사용자 정보 로드
     * 로그인 시 데이터베이스에서 사용자를 조회하고 Spring Security UserDetails 객체로 변환
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== 로그인 시도 시작 ===");
        log.info("입력된 사용자명: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.info("데이터베이스에서 사용자 발견");
        log.info("- 사용자명: {}", user.getUsername());
        log.info("- 권한: {}", user.getRole());
        log.info("- 활성화 여부: {}", user.isEnabled());
        log.info("- 저장된 비밀번호 (해시): {}", user.getPassword());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                .disabled(!user.isEnabled())
                .build();

        log.info("UserDetails 객체 생성 완료");
        log.info("=== 로그인 시도 종료 ===");

        return userDetails;
    }
}
