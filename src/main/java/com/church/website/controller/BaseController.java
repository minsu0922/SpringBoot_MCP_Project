package com.church.website.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 모든 Controller가 공통으로 상속하는 추상 기반 클래스.
 *
 * <p>예외 처리 로직을 두 헬퍼 메서드로 캡슐화하여 각 컨트롤러에서
 * try-catch를 반복 작성하지 않아도 된다.
 *
 * <ul>
 *   <li>{@link #run} : POST 처리 후 다른 URL로 redirect할 때 사용.
 *       예외 발생 시 flash 메시지를 담아 errorRedirect 경로로 이동한다.</li>
 *   <li>{@link #runWithModel} : redirect 없이 같은 뷰에 오류를 표시해야 할 때 사용.
 *       폼 입력 오류처럼 입력 화면을 그대로 다시 보여주는 경우에 적합하다.</li>
 * </ul>
 *
 * <p>예외 구분 규칙:
 * <ul>
 *   <li>IllegalArgumentException → 비밀번호 불일치처럼 서비스 레이어가 의도적으로
 *       던지는 비즈니스 오류. 내부 메시지를 사용자에게 그대로 표시한다.</li>
 *   <li>Exception → DB 오류 등 예상치 못한 시스템 오류. 사용자에게는
 *       일반 안내 메시지만 보여주고, 상세 원인은 서버 로그에만 기록한다.</li>
 * </ul>
 */
@Slf4j
public abstract class BaseController {

    /** 람다로 컨트롤러 로직 한 블록을 전달하기 위한 함수형 인터페이스. */
    @FunctionalInterface
    protected interface AdminAction {
        String execute() throws Exception;
    }

    /**
     * POST 핸들러용 예외 처리 헬퍼 — 처리 결과에 따라 다른 URL로 redirect한다.
     *
     * <p>예외 종류별 처리:
     * <ul>
     *   <li>IllegalArgumentException: 서비스가 의도적으로 던지는 비즈니스 규칙 위반
     *       (비밀번호 불일치, 중복 계정 등) → getMessage()를 사용자에게 그대로 노출</li>
     *   <li>Exception: DB 오류, IO 오류 등 예상치 못한 시스템 예외
     *       → 내부 오류 메시지는 숨기고 로그로만 기록. 사용자에게는 일반 메시지 출력</li>
     * </ul>
     *
     * @param action        실행할 컨트롤러 로직 (람다)
     * @param errorRedirect 예외 발생 시 이동할 경로 (예: "/admin/notices")
     * @param logMsg        서버 로그에 남길 오류 메시지 앞부분
     * @param ra            flash 메시지를 담을 RedirectAttributes
     * @return 이동할 redirect 경로 문자열
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
     * 공개 폼 액션용 예외 처리 헬퍼 — redirect 없이 같은 뷰에 오류를 표시한다.
     *
     * <p>새가족 등록처럼 오류가 생겨도 작성한 폼 내용을 유지한 채
     * 같은 페이지를 다시 보여줘야 할 때 사용한다.
     *
     * @param action    실행할 로직 (람다)
     * @param errorView 예외 발생 시 렌더링할 뷰 이름 (예: "new-family/form")
     * @param logMsg    서버 로그에 남길 오류 메시지 앞부분
     * @param model     오류 메시지를 뷰에 전달하기 위한 Model
     * @return 렌더링할 뷰 이름 문자열
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
