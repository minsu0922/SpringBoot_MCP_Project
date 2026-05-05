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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/about/**", "/worship/**", "/notice/**", "/location",
                    "/ministry/**", "/new-family/**", "/sermon/**", "/login",
                    "/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .frameOptions(fo -> fo.deny())
                .contentTypeOptions(cto -> {})
                .referrerPolicy(rp -> rp
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' dapi.kakao.com; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: blob: *; " +
                        "frame-src 'self' www.youtube.com youtube.com; " +
                        "connect-src 'self'"
                    )
                )
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .successHandler((request, response, authentication) -> {
                    String username = authentication.getName();
                    loginAttemptService.loginSucceeded(username);
                    log.info("로그인 성공: {}", username);
                    response.sendRedirect("/admin");
                })
                .failureHandler((request, response, exception) -> {
                    String username = request.getParameter("username");
                    if (username != null) {
                        loginAttemptService.loginFailed(username);
                    }
                    log.warn("로그인 실패 시도 감지: {}", username);
                    if (username != null && loginAttemptService.isBlocked(username)) {
                        response.sendRedirect("/login?blocked=true");
                    } else {
                        response.sendRedirect("/login?error=true");
                    }
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
