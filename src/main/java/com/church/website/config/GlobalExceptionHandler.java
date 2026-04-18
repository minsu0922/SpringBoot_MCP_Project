package com.church.website.config;

import com.church.website.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 전역 예외 핸들러
 *
 * BaseController.run()과 역할 분담:
 *   - run()       : POST 핸들러 내부에서 발생하는 예외 → flash 메시지 + redirect 처리
 *   - 이 클래스   : GET 핸들러(상세 조회 등)나 run() 밖에서 발생하는 예외 → 에러 페이지 렌더링
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 존재하지 않는 리소스 접근 (예: /admin/notices/edit/9999)
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    // 예상치 못한 모든 예외의 최후 방어선
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        log.error("처리되지 않은 예외 발생", ex);
        model.addAttribute("message", "서버 오류가 발생했습니다.");
        return "error/500";
    }
}
