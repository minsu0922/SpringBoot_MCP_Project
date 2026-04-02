package com.church.website.controller;

import com.church.website.entity.NewFamily;
import com.church.website.service.NewFamilyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 새가족 등록 컨트롤러 (공개 폼)
 */
@Slf4j
@Controller
@RequestMapping("/new-family")
@RequiredArgsConstructor
public class NewFamilyController {

    private final NewFamilyService newFamilyService;

    /**
     * 새가족 등록 폼
     */
    @GetMapping("")
    public String form(Model model) {
        model.addAttribute("newFamily", new NewFamily());
        return "new-family/form";
    }

    /**
     * 새가족 등록 처리
     */
    @PostMapping("/submit")
    public String submit(@Valid @ModelAttribute NewFamily newFamily,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return "new-family/form";
        }
        try {
            newFamilyService.register(newFamily);
            return "redirect:/new-family/complete";
        } catch (Exception e) {
            log.error("새가족 등록 실패", e);
            model.addAttribute("error", "등록 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return "new-family/form";
        }
    }

    /**
     * 등록 완료 페이지
     */
    @GetMapping("/complete")
    public String complete() {
        return "new-family/complete";
    }
}
