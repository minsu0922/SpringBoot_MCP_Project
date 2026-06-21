package com.church.website.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 브라우저 파비콘 요청 처리 컨트롤러.
 *
 * <p>브라우저는 모든 페이지 접속 시 자동으로 /favicon.ico 를 요청한다.
 * 이 프로젝트는 파비콘을 .ico 대신 .svg 형식으로 제공하므로,
 * /favicon.ico 요청을 /favicon.svg 로 redirect한다.
 *
 *
 *
 *
 *
 * <p>이 처리를 하지 않으면 /favicon.ico 요청이 404로 끝나
 * 서버 로그에 불필요한 오류가 계속 쌓인다.
 */
@Controller
public class FaviconController {

    /** /favicon.ico 요청을 실제 파비콘 파일인 /favicon.svg 로 redirect한다. */
    @GetMapping("favicon.ico")
    public String favicon() {
        return "redirect:/favicon.svg";
    }
}
