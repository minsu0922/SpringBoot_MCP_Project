package com.church.website.controller;

import com.church.website.entity.MainImage;
import com.church.website.entity.Notice;
import com.church.website.service.MainImageService;
import com.church.website.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * 관리자 페이지 컨트롤러
 * ADMIN 권한이 필요한 관리자 페이지 라우팅 처리
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final NoticeService noticeService;
    private final MainImageService mainImageService;

    /**
     * 관리자 메인 페이지
     */
    @GetMapping("")
    public String adminMain() {
        return "admin/index";
    }

    // ====== 공지사항 관리 ======

    /**
     * 공지사항 목록
     */
    @GetMapping("/notices")
    public String noticeList(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        return "admin/notice/list";
    }

    /**
     * 공지사항 작성 폼
     */
    @GetMapping("/notices/new")
    public String noticeForm(Model model) {
        model.addAttribute("notice", new Notice());
        return "admin/notice/form";
    }

    /**
     * 공지사항 수정 폼
     */
    @GetMapping("/notices/edit/{id}")
    public String noticeEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeService.getNoticeById(id));
        return "admin/notice/form";
    }

    /**
     * 공지사항 등록/수정 처리
     */
    @PostMapping("/notices/save")
    public String noticeSave(
            @ModelAttribute Notice notice,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime popupStartDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime popupEndDate,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            notice.setPopupStartDate(popupStartDate);
            notice.setPopupEndDate(popupEndDate);

            if (notice.getId() == null) {
                notice.setAuthor(auth.getName());
                noticeService.createNotice(notice);
                redirectAttributes.addFlashAttribute("message", "공지사항이 등록되었습니다.");
            } else {
                noticeService.updateNotice(notice.getId(), notice);
                redirectAttributes.addFlashAttribute("message", "공지사항이 수정되었습니다.");
            }
            return "redirect:/admin/notices";
        } catch (Exception e) {
            log.error("공지사항 저장 실패", e);
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다.");
            return "redirect:/admin/notices";
        }
    }

    /**
     * 공지사항 삭제
     */
    @PostMapping("/notices/delete/{id}")
    public String noticeDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            noticeService.deleteNotice(id);
            redirectAttributes.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("공지사항 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/notices";
    }

    // ====== 메인 이미지 관리 ======

    /**
     * 메인 이미지 목록
     */
    @GetMapping("/images")
    public String imageList(Model model) {
        model.addAttribute("images", mainImageService.getAllImages());
        return "admin/image/list";
    }

    /**
     * 메인 이미지 등록 폼
     */
    @GetMapping("/images/new")
    public String imageForm(Model model) {
        model.addAttribute("image", new MainImage());
        return "admin/image/form";
    }

    /**
     * 메인 이미지 수정 폼
     */
    @GetMapping("/images/edit/{id}")
    public String imageEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("image", mainImageService.getImageById(id));
        return "admin/image/form";
    }

    /**
     * 메인 이미지 등록/수정 처리
     */
    @PostMapping("/images/save")
    public String imageSave(@ModelAttribute MainImage image, RedirectAttributes redirectAttributes) {
        try {
            if (image.getId() == null) {
                // 신규 등록
                mainImageService.createImage(image);
                redirectAttributes.addFlashAttribute("message", "이미지가 등록되었습니다.");
            } else {
                // 수정
                mainImageService.updateImage(image.getId(), image);
                redirectAttributes.addFlashAttribute("message", "이미지가 수정되었습니다.");
            }
            return "redirect:/admin/images";
        } catch (Exception e) {
            log.error("이미지 저장 실패", e);
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다.");
            return "redirect:/admin/images";
        }
    }

    /**
     * 메인 이미지 삭제
     */
    @PostMapping("/images/delete/{id}")
    public String imageDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            mainImageService.deleteImage(id);
            redirectAttributes.addFlashAttribute("message", "이미지가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("이미지 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/images";
    }
}
