package com.church.website.controller;

import com.church.website.entity.Location;
import com.church.website.entity.MinistryPhoto;
import com.church.website.entity.NewFamily;
import com.church.website.entity.Notice;
import com.church.website.entity.Sermon;
import com.church.website.entity.User;
import com.church.website.service.LocationService;
import com.church.website.service.MinistryPhotoService;
import com.church.website.service.NewFamilyService;
import com.church.website.service.NoticeService;
import com.church.website.service.SermonService;
import com.church.website.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController extends BaseController {

    private final NoticeService noticeService;
    private final NewFamilyService newFamilyService;
    private final MinistryPhotoService ministryPhotoService;
    private final UserService userService;
    private final LocationService locationService;
    private final SermonService sermonService;

    // ====== 공통: 사이드바 뱃지용 미확인 건수 ======

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("uncheckedCount", newFamilyService.getUncheckedCount());
    }

    // ====== 대시보드 ======

    @GetMapping("")
    public String adminMain(Model model) {
        List<Notice> all = noticeService.getAllNotices();
        model.addAttribute("noticeCount",    all.size());
        model.addAttribute("ministryCount",  ministryPhotoService.getAllPhotos().size());
        model.addAttribute("newFamilyTotal", newFamilyService.getAll().size());
        model.addAttribute("recentNotices",  all.stream().limit(5).collect(Collectors.toList()));
        model.addAttribute("recentNewFamilies",
            newFamilyService.getAll().stream()
                .filter(nf -> !nf.isChecked())
                .limit(5)
                .collect(Collectors.toList()));
        return "admin/index";
    }

    // ====== 공지사항 관리 ======

    @GetMapping("/notices")
    public String noticeList(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String popupStatus,
            Model model) {
        List<Notice> all = noticeService.getAllNotices();
        int totalCount = all.size();
        List<Notice> notices = all;
        if (!keyword.isEmpty()) {
            notices = notices.stream()
                .filter(n -> n.getTitle().contains(keyword))
                .collect(Collectors.toList());
        }
        if ("on".equals(popupStatus)) {
            notices = notices.stream().filter(Notice::isPopup).collect(Collectors.toList());
        } else if ("off".equals(popupStatus)) {
            notices = notices.stream().filter(n -> !n.isPopup()).collect(Collectors.toList());
        }
        model.addAttribute("notices",     notices);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("popupStatus", popupStatus);
        model.addAttribute("totalCount",  totalCount);
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
        return run(() -> {
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
        }, "/admin/notices", "공지사항 저장 실패", redirectAttributes);
    }

    @PostMapping("/notices/delete/{id}")
    public String noticeDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return run(() -> {
            noticeService.deleteNotice(id);
            redirectAttributes.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
            return "redirect:/admin/notices";
        }, "/admin/notices", "공지사항 삭제 실패", redirectAttributes);
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
                               @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                               RedirectAttributes redirectAttributes) {
        return run(() -> {
            photo.setIsActive("true".equals(isActiveStr));
            String uploadedUrl = (photoFile != null && !photoFile.isEmpty())
                    ? ministryPhotoService.saveFile(photoFile) : null;
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
        }, "/admin/ministry", "사역사진 저장 실패", redirectAttributes);
    }

    @PostMapping("/ministry/order/{id}")
    public String ministryOrder(@PathVariable Long id,
                                @RequestParam int displayOrder,
                                RedirectAttributes redirectAttributes) {
        return run(() -> {
            MinistryPhoto photo = ministryPhotoService.getPhotoById(id);
            photo.setDisplayOrder(displayOrder);
            ministryPhotoService.updatePhoto(id, photo, null);
            redirectAttributes.addFlashAttribute("message", "표시 순서가 변경되었습니다.");
            return "redirect:/admin/ministry";
        }, "/admin/ministry", "사역사진 순서 변경 실패", redirectAttributes);
    }

    @PostMapping("/ministry/delete/{id}")
    public String ministryDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return run(() -> {
            MinistryPhoto photo = ministryPhotoService.getPhotoById(id);
            ministryPhotoService.deleteFile(photo.getPhotoUrl());
            ministryPhotoService.deletePhoto(id);
            redirectAttributes.addFlashAttribute("message", "사역사진이 삭제되었습니다.");
            return "redirect:/admin/ministry";
        }, "/admin/ministry", "사역사진 삭제 실패", redirectAttributes);
    }

    // ====== 새가족 관리 ======

    @GetMapping("/new-family")
    public String newFamilyList(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        int size = 10;
        Page<NewFamily> result = newFamilyService.search(keyword, status, page, size);
        model.addAttribute("list",          result.getContent());
        model.addAttribute("currentPage",   result.getNumber());
        model.addAttribute("totalPages",    result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("keyword",       keyword);
        model.addAttribute("status",        status);
        model.addAttribute("checkedCount",  newFamilyService.getCheckedCount());
        return "admin/new-family/list";
    }

    @GetMapping("/new-family/{id}")
    public String newFamilyDetail(@PathVariable Long id, Model model) {
        model.addAttribute("nf", newFamilyService.getById(id));
        return "admin/new-family/detail";
    }

    @PostMapping("/new-family/check/{id}")
    public String newFamilyCheck(@PathVariable Long id,
                                 @RequestParam(defaultValue = "") String keyword,
                                 @RequestParam(defaultValue = "") String status,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "list") String from,
                                 RedirectAttributes redirectAttributes) {
        return run(() -> {
            newFamilyService.check(id);
            redirectAttributes.addFlashAttribute("message", "확인 처리되었습니다.");
            return "detail".equals(from)
                    ? "redirect:/admin/new-family/" + id
                    : "redirect:/admin/new-family?keyword=" + keyword + "&status=" + status + "&page=" + page;
        }, "/admin/new-family", "새가족 확인 처리 실패", redirectAttributes);
    }

    @PostMapping("/new-family/uncheck/{id}")
    public String newFamilyUncheck(@PathVariable Long id,
                                   @RequestParam(defaultValue = "") String keyword,
                                   @RequestParam(defaultValue = "") String status,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "list") String from,
                                   RedirectAttributes redirectAttributes) {
        return run(() -> {
            newFamilyService.uncheck(id);
            redirectAttributes.addFlashAttribute("message", "확인이 취소되었습니다.");
            return "detail".equals(from)
                    ? "redirect:/admin/new-family/" + id
                    : "redirect:/admin/new-family?keyword=" + keyword + "&status=" + status + "&page=" + page;
        }, "/admin/new-family", "새가족 확인 취소 실패", redirectAttributes);
    }

    @PostMapping("/new-family/admin-memo/{id}")
    public String newFamilyAdminMemo(@PathVariable Long id,
                                     @RequestParam(defaultValue = "") String adminMemo,
                                     RedirectAttributes redirectAttributes) {
        return run(() -> {
            newFamilyService.saveAdminMemo(id, adminMemo);
            redirectAttributes.addFlashAttribute("message", "메모가 저장되었습니다.");
            return "redirect:/admin/new-family/" + id;
        }, "/admin/new-family/" + id, "새가족 메모 저장 실패", redirectAttributes);
    }

    @PostMapping("/new-family/delete/{id}")
    public String newFamilyDelete(@PathVariable Long id,
                                  @RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "") String status,
                                  @RequestParam(defaultValue = "0") int page,
                                  RedirectAttributes redirectAttributes) {
        return run(() -> {
            newFamilyService.delete(id);
            redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
            return "redirect:/admin/new-family?keyword=" + keyword + "&status=" + status + "&page=" + page;
        }, "/admin/new-family", "새가족 삭제 실패", redirectAttributes);
    }

    // ====== 설교 관리 ======

    @GetMapping("/sermon")
    public String sermonList(Model model) {
        model.addAttribute("sermons", sermonService.getAll());
        return "admin/sermon/list";
    }

    @GetMapping("/sermon/new")
    public String sermonForm(Model model) {
        model.addAttribute("sermon", new Sermon());
        return "admin/sermon/form";
    }

    @GetMapping("/sermon/edit/{id}")
    public String sermonEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("sermon", sermonService.getById(id));
        return "admin/sermon/form";
    }

    @PostMapping("/sermon/save")
    public String sermonSave(
            @ModelAttribute Sermon sermon,
            @RequestParam(value = "sermonDateStr", required = false) String sermonDateStr,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            RedirectAttributes redirectAttributes) {
        return run(() -> {
            if (sermonDateStr != null && !sermonDateStr.isBlank()) {
                sermon.setSermonDate(LocalDate.parse(sermonDateStr));
            }
            String newVideoUrl     = (videoFile     != null && !videoFile.isEmpty())     ? sermonService.saveFile(videoFile,     "video") : null;
            String newThumbnailUrl = (thumbnailFile != null && !thumbnailFile.isEmpty()) ? sermonService.saveFile(thumbnailFile, "thumb") : null;
            if (sermon.getId() == null) {
                if (newVideoUrl == null) {
                    redirectAttributes.addFlashAttribute("error", "동영상 파일을 선택해주세요.");
                    return "redirect:/admin/sermon/new";
                }
                sermon.setVideoUrl(newVideoUrl);
                sermon.setThumbnailUrl(newThumbnailUrl);
                sermonService.create(sermon);
                redirectAttributes.addFlashAttribute("message", "설교가 등록되었습니다.");
            } else {
                sermonService.update(sermon.getId(), sermon, newVideoUrl, newThumbnailUrl);
                redirectAttributes.addFlashAttribute("message", "설교가 수정되었습니다.");
            }
            return "redirect:/admin/sermon";
        }, "/admin/sermon", "설교 저장 실패", redirectAttributes);
    }

    @PostMapping("/sermon/delete/{id}")
    public String sermonDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return run(() -> {
            sermonService.delete(id);
            redirectAttributes.addFlashAttribute("message", "설교가 삭제되었습니다.");
            return "redirect:/admin/sermon";
        }, "/admin/sermon", "설교 삭제 실패", redirectAttributes);
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
        return run(() -> {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
                return "redirect:/admin/account";
            }
            userService.changePassword(auth.getName(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
            return "redirect:/admin/account";
        }, "/admin/account", "비밀번호 변경 실패", redirectAttributes);
    }

    @PostMapping("/account/create")
    public String createAccount(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        return run(() -> {
            userService.createUser(username, password);
            redirectAttributes.addFlashAttribute("message", "계정이 추가되었습니다: " + username);
            return "redirect:/admin/account";
        }, "/admin/account", "계정 추가 실패", redirectAttributes);
    }

    @PostMapping("/account/toggle/{id}")
    public String toggleAccount(@PathVariable Long id, Authentication auth,
                                RedirectAttributes redirectAttributes) {
        return run(() -> {
            User target = userService.getUserById(id);
            if (target.getUsername().equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("error", "현재 로그인 계정은 비활성화할 수 없습니다.");
                return "redirect:/admin/account";
            }
            userService.toggleEnabled(id);
            redirectAttributes.addFlashAttribute("message", "계정 상태가 변경되었습니다.");
            return "redirect:/admin/account";
        }, "/admin/account", "계정 상태 변경 실패", redirectAttributes);
    }

    @PostMapping("/account/delete/{id}")
    public String deleteAccount(@PathVariable Long id, Authentication auth,
                                RedirectAttributes redirectAttributes) {
        return run(() -> {
            User target = userService.getUserById(id);
            if (target.getUsername().equals(auth.getName())) {
                redirectAttributes.addFlashAttribute("error", "현재 로그인 계정은 삭제할 수 없습니다.");
                return "redirect:/admin/account";
            }
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("message", "계정이 삭제되었습니다.");
            return "redirect:/admin/account";
        }, "/admin/account", "계정 삭제 실패", redirectAttributes);
    }

    // ====== 설정 ======

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("location", locationService.getActiveLocation().orElse(new Location()));
        return "admin/settings";
    }

    @PostMapping("/settings/save")
    public String settingsSave(@ModelAttribute Location location, RedirectAttributes redirectAttributes) {
        return run(() -> {
            locationService.save(location);
            redirectAttributes.addFlashAttribute("message", "교회 정보가 저장되었습니다.");
            return "redirect:/admin/settings";
        }, "/admin/settings", "설정 저장 실패", redirectAttributes);
    }
}
