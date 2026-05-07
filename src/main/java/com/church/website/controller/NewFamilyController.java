package com.church.website.controller;

import com.church.website.entity.NewFamily;
import com.church.website.service.NewFamilyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 방문자용 새가족 등록 폼 컨트롤러.
 *
 * <p>/new-family 경로 아래에서 새가족 등록 폼 표시·제출·완료 페이지를 처리한다.
 * 로그인 없이 방문자 누구나 접근할 수 있다.
 *
 * <p>흐름:
 * <ol>
 *   <li>GET /new-family → 등록 폼 표시</li>
 *   <li>POST /new-family/submit → 유효성 검사 → DB 저장 → 완료 페이지로 redirect</li>
 *   <li>GET /new-family/complete → 등록 완료 안내 페이지</li>
 * </ol>
 *
 * <p>유효성 검사: NewFamily 엔티티에 선언된 @NotBlank, @Pattern 등의 Bean Validation 애너테이션을
 * @Valid가 자동으로 검사한다. 오류가 있으면 redirect 없이 폼을 다시 보여준다.
 */
@Controller
@RequestMapping("/new-family")
@RequiredArgsConstructor
public class NewFamilyController extends BaseController {

    private final NewFamilyService newFamilyService;

    /** 새가족 등록 폼 표시. 빈 NewFamily 객체를 Model에 담아 Thymeleaf 바인딩에 사용한다. */
    @GetMapping("")
    public String form(Model model) {
        model.addAttribute("newFamily", new NewFamily());
        return "new-family/form";
    }

    /**
     * 새가족 등록 제출.
     *
     * <p>@Valid가 입력값을 검증하고 오류가 있으면 BindingResult에 담는다.
     * 오류가 있으면 폼을 다시 보여주고, 없으면 DB에 저장 후 완료 페이지로 redirect한다.
     * redirect 없이 같은 뷰에 시스템 오류를 표시해야 하므로 {@link #runWithModel}을 사용한다.
     */
    @PostMapping("/submit")
    public String submit(@Valid @ModelAttribute NewFamily newFamily,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "new-family/form";
        }
        return runWithModel(() -> {
            newFamilyService.register(newFamily);
            return "redirect:/new-family/complete";
        }, "new-family/form", "새가족 등록 실패", model);
    }

    /** 등록 완료 안내 페이지. Post-Redirect-Get 패턴으로 새로고침 시 중복 제출을 방지한다. */
    @GetMapping("/complete")
    public String complete() {
        return "new-family/complete";
    }
}
