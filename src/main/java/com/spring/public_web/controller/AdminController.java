package com.spring.public_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 페이지 컨트롤러
 * ADMIN 권한이 필요한 관리자 페이지 라우팅 처리
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    
    /**
     * 관리자 메인 페이지
     * 관리자 대시보드 및 관리 기능 페이지를 반환
     */
    @GetMapping("")
    public String adminMain() {
        return "admin/index";
    }
}
