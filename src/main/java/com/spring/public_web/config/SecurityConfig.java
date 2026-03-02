package com.spring.public_web.config;

import com.spring.public_web.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 * 웹 애플리케이션의 보안 정책(인증, 인가, CSRF)을 정의하고 관리
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * HTTP 보안 필터 체인 설정
     * URL별 접근 권한, 로그인/로그아웃 처리, CSRF 설정 등을 구성
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("=== SecurityFilterChain 설정 시작 ===");
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/about/**", "/worship/**", "/notice/**", "/location", "/login", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()  // 일반 페이지, 정적 리소스, H2 콘솔 허용
                .requestMatchers("/admin/**").hasRole("ADMIN")  // 관리자 페이지는 ADMIN 역할 필요
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")  // H2 콘솔은 CSRF 비활성화
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())  // H2 콘솔 프레임 허용
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin", true)
                .failureUrl("/login?error=true")
                .permitAll()
                .successHandler((request, response, authentication) -> {
                    log.info("=== 로그인 성공 ===");
                    log.info("로그인 사용자: {}", authentication.getName());
                    log.info("권한: {}", authentication.getAuthorities());
                    response.sendRedirect("/admin");
                })
                .failureHandler((request, response, exception) -> {
                    log.error("=== 로그인 실패 ===");
                    log.error("사용자명: {}", request.getParameter("username"));
                    log.error("실패 사유: {}", exception.getMessage());
                    response.sendRedirect("/login?error=true");
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .permitAll()
            );

        log.info("=== SecurityFilterChain 설정 완료 ===");
        return http.build();
    }

    /**
     * 비밀번호 암호화 인코더 빈 등록
     * BCrypt 알고리즘을 사용하여 비밀번호를 안전하게 해시화
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("BCryptPasswordEncoder 빈 생성");
        return new BCryptPasswordEncoder();
    }
}
