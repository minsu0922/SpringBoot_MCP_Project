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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("로그인 시도 - 존재하지 않는 계정");
                    return new UsernameNotFoundException("User not found");
                });

        log.debug("사용자 인증 처리: username={}, enabled={}", user.getUsername(), user.isEnabled());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                .disabled(!user.isEnabled())
                .build();
    }
}
