package com.church.website.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
public abstract class BaseController {

    @FunctionalInterface
    protected interface AdminAction {
        String execute() throws Exception;
    }

    /**
     * 관리자 액션 실행 헬퍼 (redirect 기반)
     *
     * IllegalArgumentException: 비밀번호 불일치, 중복 계정 등 비즈니스 규칙 위반.
     *   → 서비스가 의도적으로 던지는 예외이므로 getMessage()를 사용자에게 그대로 노출.
     *
     * Exception: DB 오류, IO 오류 등 예상치 못한 시스템 예외.
     *   → 내부 오류 메시지는 숨기고 로그로만 기록. 사용자에게는 일반 메시지 출력.
     */
    protected String run(AdminAction action, String errorRedirect, String logMsg, RedirectAttributes ra) {
        try {
            return action.execute();
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:" + errorRedirect;
        } catch (Exception e) {
            log.error(logMsg, e);
            ra.addFlashAttribute("error", "처리 중 오류가 발생했습니다.");
            return "redirect:" + errorRedirect;
        }
    }

    /**
     * 공개 폼 액션 실행 헬퍼 (view 반환 기반)
     * redirect 없이 같은 페이지에 오류를 표시해야 하는 경우(새가족 등록 등)에 사용.
     */
    protected String runWithModel(AdminAction action, String errorView, String logMsg, Model model) {
        try {
            return action.execute();
        } catch (Exception e) {
            log.error(logMsg, e);
            model.addAttribute("error", "처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return errorView;
        }
    }
}
