package com.church.website.controller;

import com.church.website.entity.MainImage;
import com.church.website.entity.MinistryPhoto;
import com.church.website.entity.Notice;
import com.church.website.service.MainImageService;
import com.church.website.service.MinistryPhotoService;
import com.church.website.service.NewFamilyService;
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
    private final NewFamilyService newFamilyService;
    private final MinistryPhotoService ministryPhotoService;

    /**
     * 관리자 메인 페이지 (대시보드)
     */
    @GetMapping("")
    public String adminMain(Model model) {
        // 통계
        model.addAttribute("noticeCount",    noticeService.getAllNotices().size());
        model.addAttribute("ministryCount",  ministryPhotoService.getAllPhotos().size());
        model.addAttribute("imageCount",     mainImageService.getAllImages().size());
        model.addAttribute("uncheckedCount", newFamilyService.getUncheckedCount());

        // 최근 공지사항 5건
        java.util.List<Notice> all = noticeService.getAllNotices();
        model.addAttribute("recentNotices",
            all.stream().limit(5).collect(java.util.stream.Collectors.toList()));

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
     * 공지사항 수정 폼 (조회수 증가 없음)
     */
    @GetMapping("/notices/edit/{id}")
    public String noticeEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeService.getNoticeByIdNoCount(id));
        return "admin/notice/form";
    }

    /**
     * 공지사항 등록/수정 처리
     */
    @PostMapping("/notices/save")
    public String noticeSave(
            @ModelAttribute Notice notice,
            @RequestParam(value = "popupEnabled", required = false) String popupEnabled,
            @RequestParam(value = "popupStartDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime popupStartDate,
            @RequestParam(value = "popupEndDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime popupEndDate,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            // @ModelAttribute의 popup 자동바인딩과 분리된 별도 파라미터로 처리
            notice.setPopup("true".equals(popupEnabled));
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

    // ====== 사역사진 관리 ======

    /**
     * 사역사진 목록
     */
    @GetMapping("/ministry")
    public String ministryList(Model model) {
        model.addAttribute("photos", ministryPhotoService.getAllPhotos());
        return "admin/ministry/list";
    }

    /**
     * 사역사진 등록 폼
     */
    @GetMapping("/ministry/new")
    public String ministryForm(Model model) {
        model.addAttribute("photo", new MinistryPhoto());
        return "admin/ministry/form";
    }

    /**
     * 사역사진 수정 폼
     */
    @GetMapping("/ministry/edit/{id}")
    public String ministryEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("photo", ministryPhotoService.getPhotoById(id));
        return "admin/ministry/form";
    }

    /**
     * 사역사진 등록/수정 처리
     */
    @PostMapping("/ministry/save")
    public String ministrySave(@ModelAttribute MinistryPhoto photo,
                               @RequestParam(value = "isActive", required = false) String isActiveStr,
                               RedirectAttributes redirectAttributes) {
        try {
            photo.setIsActive("true".equals(isActiveStr));
            if (photo.getId() == null) {
                ministryPhotoService.createPhoto(photo);
                redirectAttributes.addFlashAttribute("message", "사역사진이 등록되었습니다.");
            } else {
                ministryPhotoService.updatePhoto(photo.getId(), photo);
                redirectAttributes.addFlashAttribute("message", "사역사진이 수정되었습니다.");
            }
            return "redirect:/admin/ministry";
        } catch (Exception e) {
            log.error("사역사진 저장 실패", e);
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다.");
            return "redirect:/admin/ministry";
        }
    }

    /**
     * 사역사진 삭제
     */
    @PostMapping("/ministry/delete/{id}")
    public String ministryDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ministryPhotoService.deletePhoto(id);
            redirectAttributes.addFlashAttribute("message", "사역사진이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("사역사진 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/ministry";
    }

    // ====== 새가족 관리 ======

    /**
     * 새가족 목록
     */
    @GetMapping("/new-family")
    public String newFamilyList(Model model) {
        model.addAttribute("list", newFamilyService.getAll());
        model.addAttribute("uncheckedCount", newFamilyService.getUncheckedCount());
        return "admin/new-family/list";
    }

    /**
     * 새가족 확인 처리
     */
    @PostMapping("/new-family/check/{id}")
    public String newFamilyCheck(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newFamilyService.check(id);
            redirectAttributes.addFlashAttribute("message", "확인 처리되었습니다.");
        } catch (Exception e) {
            log.error("새가족 확인 처리 실패", e);
            redirectAttributes.addFlashAttribute("error", "처리 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/new-family";
    }

    /**
     * 새가족 삭제
     */
    @PostMapping("/new-family/delete/{id}")
    public String newFamilyDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newFamilyService.delete(id);
            redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
        } catch (Exception e) {
            log.error("새가족 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/new-family";
    }
}
