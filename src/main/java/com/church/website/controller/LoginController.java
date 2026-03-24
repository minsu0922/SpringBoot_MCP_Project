package com.church.website.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 로그인 페이지 컨트롤러
 * 사용자 로그인 화면을 제공하는 컨트롤러
 */
@Controller
public class LoginController {
    
    /**
     * 로그인 페이지
     * 사용자 인증을 위한 로그인 폼 페이지를 반환
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
