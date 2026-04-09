package com.church.website.controller;

import com.church.website.entity.Location;
import com.church.website.entity.MinistryPhoto;
import com.church.website.entity.Notice;
import com.church.website.service.LocationService;
import com.church.website.service.MinistryPhotoService;
import com.church.website.service.NewFamilyService;
import com.church.website.service.NoticeService;
import com.church.website.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final NoticeService noticeService;
    private final NewFamilyService newFamilyService;
    private final MinistryPhotoService ministryPhotoService;
    private final UserService userService;
    private final LocationService locationService;

    // ====== 대시보드 ======

    @GetMapping("")
    public String adminMain(Model model) {
        model.addAttribute("noticeCount",    noticeService.getAllNotices().size());
        model.addAttribute("ministryCount",  ministryPhotoService.getAllPhotos().size());
        model.addAttribute("uncheckedCount", newFamilyService.getUncheckedCount());
        java.util.List<Notice> all = noticeService.getAllNotices();
        model.addAttribute("recentNotices",
            all.stream().limit(5).collect(java.util.stream.Collectors.toList()));
        return "admin/index";
    }

    // ====== 공지사항 관리 ======

    @GetMapping("/notices")
    public String noticeList(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        return "admin/notice/list";
    }

    @GetMapping("/notices/new")
    public String noticeForm(Model model) {
        model.addAttribute("notice", new Notice());
        return "admin/notice/form";
    }

    @GetMapping("/notices/edit/{id}")
    public String noticeEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeService.getNoticeByIdNoCount(id));
        return "admin/notice/form";
    }

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

    // ====== 사역사진 관리 ======

    @GetMapping("/ministry")
    public String ministryList(Model model) {
        model.addAttribute("photos", ministryPhotoService.getAllPhotos());
        return "admin/ministry/list";
    }

    @GetMapping("/ministry/new")
    public String ministryForm(Model model) {
        model.addAttribute("photo", new MinistryPhoto());
        return "admin/ministry/form";
    }

    @GetMapping("/ministry/edit/{id}")
    public String ministryEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("photo", ministryPhotoService.getPhotoById(id));
        return "admin/ministry/form";
    }

    @PostMapping("/ministry/save")
    public String ministrySave(@ModelAttribute MinistryPhoto photo,
                               @RequestParam(value = "isActive", required = false) String isActiveStr,
                               @RequestParam(value = "photoFile", required = false) org.springframework.web.multipart.MultipartFile photoFile,
                               RedirectAttributes redirectAttributes) {
        try {
            photo.setIsActive("true".equals(isActiveStr));
            String uploadedUrl = null;
            if (photoFile != null && !photoFile.isEmpty()) {
                uploadedUrl = ministryPhotoService.saveFile(photoFile);
            }
            if (photo.getId() == null) {
                if (uploadedUrl == null) {
                    redirectAttributes.addFlashAttribute("error", "사진 파일을 선택해주세요.");
                    return "redirect:/admin/ministry/new";
                }
                photo.setPhotoUrl(uploadedUrl);
                ministryPhotoService.createPhoto(photo);
                redirectAttributes.addFlashAttribute("message", "사역사진이 등록되었습니다.");
            } else {
                ministryPhotoService.updatePhoto(photo.getId(), photo, uploadedUrl);
                redirectAttributes.addFlashAttribute("message", "사역사진이 수정되었습니다.");
            }
            return "redirect:/admin/ministry";
        } catch (Exception e) {
            log.error("사역사진 저장 실패", e);
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다.");
            return "redirect:/admin/ministry";
        }
    }

    @PostMapping("/ministry/delete/{id}")
    public String ministryDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            com.church.website.entity.MinistryPhoto photo = ministryPhotoService.getPhotoById(id);
            ministryPhotoService.deleteFile(photo.getPhotoUrl());
            ministryPhotoService.deletePhoto(id);
            redirectAttributes.addFlashAttribute("message", "사역사진이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("사역사진 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/ministry";
    }

    // ====== 새가족 관리 ======

    @GetMapping("/new-family")
    public String newFamilyList(Model model) {
        model.addAttribute("list", newFamilyService.getAll());
        model.addAttribute("uncheckedCount", newFamilyService.getUncheckedCount());
        return "admin/new-family/list";
    }

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

    // ====== 계정 관리 ======

    @GetMapping("/account")
    public String accountList(Model model, Authentication auth) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUsername", auth.getName());
        return "admin/account/list";
    }

    @PostMapping("/account/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
                return "redirect:/admin/account";
            }
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "비밀번호는 8자 이상이어야 합니다.");
                return "redirect:/admin/account";
            }
            userService.changePassword(auth.getName(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("비밀번호 변경 실패", e);
            redirectAttributes.addFlashAttribute("error", "변경 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/account";
    }

    @PostMapping("/account/create")
    public String createAccount(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            if (password.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "비밀번호는 8자 이상이어야 합니다.");
                return "redirect:/admin/account";
            }
            userService.createUser(username, password);
            redirectAttributes.addFlashAttribute("message", "계정이 추가되었습니다: " + username);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("계정 추가 실패", e);
            redirectAttributes.addFlashAttribute("error", "추가 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/account";
    }

    @PostMapping("/account/toggle/{id}")
    public String toggleAccount(@PathVariable Long id, Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            com.church.website.entity.User target = userService.getUserById(id);
            if (target.getUsername().equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("error", "현재 로그인 계정은 비활성화할 수 없습니다.");
                return "redirect:/admin/account";
            }
            userService.toggleEnabled(id);
            redirectAttributes.addFlashAttribute("message", "계정 상태가 변경되었습니다.");
        } catch (Exception e) {
            log.error("계정 상태 변경 실패", e);
            redirectAttributes.addFlashAttribute("error", "변경 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/account";
    }

    @PostMapping("/account/delete/{id}")
    public String deleteAccount(@PathVariable Long id, Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            com.church.website.entity.User target = userService.getUserById(id);
            if (target.getUsername().equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("error", "현재 로그인 계정은 삭제할 수 없습니다.");
                return "redirect:/admin/account";
            }
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "계정이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("계정 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/account";
    }

    // ====== 설정 ======

    @GetMapping("/settings")
    public String settings(Model model) {
        Location location = locationService.getActiveLocation().orElse(new Location());
        model.addAttribute("location", location);
        return "admin/settings";
    }

    @PostMapping("/settings/save")
    public String settingsSave(@ModelAttribute Location location,
                               RedirectAttributes redirectAttributes) {
        try {
            locationService.save(location);
            redirectAttributes.addFlashAttribute("message", "교회 정보가 저장되었습니다.");
        } catch (Exception e) {
            log.error("설정 저장 실패", e);
            redirectAttributes.addFlashAttribute("error", "저장 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/settings";
    }
}
