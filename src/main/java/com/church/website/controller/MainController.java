package com.church.website.controller;

import com.church.website.entity.MinistryPhoto;
import com.church.website.service.LocationService;
import com.church.website.service.MainImageService;
import com.church.website.service.MinistryPhotoService;
import com.church.website.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 일반 사용자용 메인 컨트롤러
 * 홈, 소개, 예배안내, 공지사항, 오시는길 등 공개 페이지 라우팅 처리
 */
@Controller
@RequiredArgsConstructor
public class MainController {

    private final MainImageService mainImageService;
    private final NoticeService noticeService;
    private final MinistryPhotoService ministryPhotoService;
    private final LocationService locationService;

    /**
     * 홈 페이지
     * 웹사이트의 메인 페이지를 반환 (활성화된 메인 이미지 포함)
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("mainImages", mainImageService.getActiveImages());
        model.addAttribute("recentNotices", noticeService.getRecentNotices());
        model.addAttribute("popupNotices", noticeService.getActivePopups());

        List<MinistryPhoto> allPhotos = ministryPhotoService.getActivePhotos();
        model.addAttribute("mainMinistryPhotos",
                allPhotos.size() > 4 ? allPhotos.subList(0, 4) : allPhotos);

        // DB 교회 정보 → 메인 오시는 길 섹션
        locationService.getActiveLocation()
                .ifPresent(loc -> model.addAttribute("loc", loc));

        return "index";
    }

    /**
     * 교회 소개 페이지
     * 교회의 역사, 비전, 사명 등을 소개하는 페이지를 반환
     */
    @GetMapping("/about/church")
    public String aboutChurch() {
        return "about/church";
    }

    /**
     * 담임목사 소개 페이지
     * 담임목사의 프로필과 메시지를 보여주는 페이지를 반환
     */
    @GetMapping("/about/pastor")
    public String aboutPastor() {
        return "about/pastor";
    }

    /**
     * 교역자 소개 페이지
     * 교회 교역자 및 스태프 정보를 제공하는 페이지를 반환
     */
    @GetMapping("/about/staff")
    public String aboutStaff() {
        return "about/staff";
    }

    /**
     * 예배 시간 안내 페이지
     * 주일예배, 새벽기도회 등 예배 일정을 안내하는 페이지를 반환
     */
    @GetMapping("/worship/schedule")
    public String worshipSchedule() {
        return "worship/schedule";
    }

    /**
     * 부서별 예배 안내 페이지
     * 유아부, 청년부 등 각 부서별 예배 정보를 제공하는 페이지를 반환
     */
    @GetMapping("/worship/department")
    public String worshipDepartment() {
        return "worship/department";
    }

    /**
     * 공지사항 목록 페이지 (페이지네이션)
     */
    @GetMapping("/notice/list")
    public String noticeList(@RequestParam(defaultValue = "0") int page, Model model) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        model.addAttribute("notices", noticeService.getNoticesPaged(pageable));
        return "notice/list";
    }

    /**
     * 사역소개 페이지
     */
    @GetMapping("/ministry")
    public String ministryPage(Model model) {
        model.addAttribute("photos", ministryPhotoService.getActivePhotos());
        return "ministry/index";
    }

    /**
     * 공지사항 상세 페이지
     * 선택한 공지사항의 상세 내용을 보여주는 페이지를 반환
     */
    @GetMapping("/notice/detail/{id}")
    public String noticeDetail(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeService.getNoticeById(id));
        model.addAttribute("prevNotice", noticeService.getPrevNotice(id));
        model.addAttribute("nextNotice", noticeService.getNextNotice(id));
        return "notice/detail";
    }

    /**
     * 오시는 길 페이지
     * 교회 위치와 찾아오는 방법을 안내하는 페이지를 반환
     */
    @GetMapping("/location")
    public String location(Model model) {
        locationService.getActiveLocation()
                .ifPresent(loc -> model.addAttribute("loc", loc));
        return "location";
    }
}
