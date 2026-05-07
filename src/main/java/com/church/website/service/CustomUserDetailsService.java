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
 * Spring Security 로그인 인증을 위한 사용자 정보 로드 서비스.
 *
 * <p>Spring Security는 로그인 시 이 서비스의 loadUserByUsername()을 자동으로 호출한다.
 * DB에서 User 엔티티를 조회한 뒤 Spring Security가 이해하는 UserDetails 객체로 변환하여 반환한다.
 *
 * <p>동작 흐름:
 * <ol>
 *   <li>사용자가 /login 폼에 아이디·비밀번호를 입력하고 전송</li>
 *   <li>Spring Security가 loadUserByUsername(username) 호출</li>
 *   <li>이 메서드가 DB에서 User를 찾아 UserDetails로 변환하여 반환</li>
 *   <li>Spring Security가 입력된 비밀번호와 UserDetails의 해시 비밀번호를 BCrypt로 비교</li>
 *   <li>일치하면 로그인 성공 → SecurityConfig의 successHandler 실행</li>
 * </ol>
 *
 * <p>주의: enabled=false 계정은 Spring Security가 자동으로 로그인을 거부한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자명으로 DB에서 계정을 조회하여 UserDetails로 변환한다.
     *
     * <p>계정이 없으면 UsernameNotFoundException을 던지며,
     * Spring Security가 이를 잡아 로그인 실패로 처리한다.
     * 보안상 "계정 없음"과 "비밀번호 틀림"을 사용자에게 구분하여 알리지 않는다.
     *
     * @param username 로그인 폼에서 입력한 아이디
     * @return Spring Security 인증에 사용할 UserDetails 객체
     * @throws UsernameNotFoundException 해당 username의 계정이 DB에 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("로그인 시도 - 존재하지 않는 계정: {}", username);
                    return new UsernameNotFoundException("User not found");
                });

        log.debug("사용자 인증 처리: username={}, enabled={}", user.getUsername(), user.isEnabled());

        // User 엔티티 → Spring Security UserDetails 변환
        // disabled(!enabled)이면 Spring Security가 자동으로 로그인 거부
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // BCrypt 해시값이 그대로 전달됨
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                .disabled(!user.isEnabled())
                .build();
    }
}
