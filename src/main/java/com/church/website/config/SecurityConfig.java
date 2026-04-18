package com.church.website.config;

import com.church.website.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 공개 페이지 및 정적 리소스는 인증 없이 접근 허용
                .requestMatchers(
                    "/", "/about/**", "/worship/**", "/notice/**", "/location",
                    "/ministry/**", "/new-family/**", "/login",
                    "/css/**", "/js/**", "/images/**", "/uploads/**", "/h2-console/**"
                ).permitAll()
                // 관리자 페이지는 ROLE_ADMIN 권한 필수
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf
                // H2 콘솔은 iframe + 별도 form 구조라 CSRF 토큰 검증에서 제외
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                // H2 콘솔이 iframe으로 동작하므로 동일 출처(same-origin) frame 허용
                .frameOptions(frame -> frame.sameOrigin())
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                // successHandler가 설정되면 defaultSuccessUrl은 무시되므로 여기서만 리다이렉트 처리
                .successHandler((request, response, authentication) -> {
                    log.info("로그인 성공: {}", authentication.getName());
                    response.sendRedirect("/admin");
                })
                .failureHandler((request, response, exception) -> {
                    log.warn("로그인 실패: username={}", request.getParameter("username"));
                    response.sendRedirect("/login?error=true");
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
