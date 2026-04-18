package com.church.website.controller;

import com.church.website.entity.NewFamily;
import com.church.website.service.NewFamilyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/new-family")
@RequiredArgsConstructor
public class NewFamilyController extends BaseController {

    private final NewFamilyService newFamilyService;

    @GetMapping("")
    public String form(Model model) {
        model.addAttribute("newFamily", new NewFamily());
        return "new-family/form";
    }

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

    @GetMapping("/complete")
    public String complete() {
        return "new-family/complete";
    }
}
