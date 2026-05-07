package com.church.website.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 로그인 페이지 컨트롤러.
 *
 * <p>실제 로그인 처리(아이디·비밀번호 검증)는 Spring Security가 자동으로 담당하며,
 * 이 컨트롤러는 /login GET 요청에 대해 로그인 폼 페이지만 렌더링한다.
 *
 * <p>로그인 성공·실패 처리 흐름:
 * <ul>
 *   <li>성공 → SecurityConfig의 successHandler가 /admin 으로 redirect</li>
 *   <li>실패 → SecurityConfig의 failureHandler가 /login?error=true 로 redirect</li>
 *   <li>계정 잠금 → /login?blocked=true 로 redirect (LoginAttemptService 연계)</li>
 * </ul>
 */
@Controller
public class LoginController {

    /**
     * 로그인 폼 페이지를 렌더링한다.
     * 이미 로그인된 사용자가 접근해도 Spring Security가 /admin으로 자동 redirect하지 않으므로
     * 로그인 폼이 그대로 표시된다.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
