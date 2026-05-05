package com.church.website.controller;

import com.church.website.entity.MinistryPhoto;
import com.church.website.service.LocationService;
import com.church.website.service.MainImageService;
import com.church.website.service.MinistryPhotoService;
import com.church.website.service.NoticeService;
import com.church.website.service.SermonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private static final List<String[]> DAILY_VERSES = List.of(
        new String[]{ "“내가 곧 길이요 진리요 생명이니 나로 말미암지 않고는 아버지께로 올 자가 없느니라”", "요한복음 14:6" },
        new String[]{ "“여호와는 나의 목자시니 내게 부족함이 없으리로다”", "시편 23:1" },
        new String[]{ "“하나님이 세상을 이처럼 사랑하사 독생자를 주셨으니 이는 그를 믿는 자마다 멸망하지 않고 영생을 얻게 하려 하심이라”", "요한복음 3:16" },
        new String[]{ "“내게 능력 주시는 자 안에서 내가 모든 것을 할 수 있느니라”", "빌립보서 4:13" },
        new String[]{ "“너는 마음을 다하여 여호와를 신뢰하고 네 명철을 의지하지 말라”", "잠언 3:5" }
    );

    @Value("${kakao.map.api.key}")
    private String kakaoMapApiKey;

    private final MainImageService mainImageService;
    private final NoticeService noticeService;
    private final MinistryPhotoService ministryPhotoService;
    private final LocationService locationService;
    private final SermonService sermonService;

    @GetMapping("/")
    public String index(Model model) {
        // 날짜 기반으로 고정 — 같은 날 새로고침해도 같은 구절 표시
        int verseIndex = LocalDate.now().getDayOfYear() % DAILY_VERSES.size();
        String[] verse = DAILY_VERSES.get(verseIndex);
        model.addAttribute("verseText", verse[0]);
        model.addAttribute("verseRef",  verse[1]);
        model.addAttribute("mainImages", mainImageService.getActiveImages());
        model.addAttribute("recentNotices", noticeService.getRecentNotices());
        model.addAttribute("recentSermons", sermonService.getRecentSermons());
        model.addAttribute("popupNotices", noticeService.getActivePopups());
        model.addAttribute("kakaoMapApiKey", kakaoMapApiKey);

        List<MinistryPhoto> allPhotos = ministryPhotoService.getActivePhotos();
        model.addAttribute("mainMinistryPhotos",
                allPhotos.size() > 4 ? allPhotos.subList(0, 4) : allPhotos);

        locationService.getActiveLocation()
                .ifPresent(loc -> model.addAttribute("loc", loc));

        return "index";
    }

    @GetMapping("/about/church")
    public String aboutChurch() {
        return "about/church";
    }

    @GetMapping("/about/pastor")
    public String aboutPastor() {
        return "about/pastor";
    }

    @GetMapping("/about/staff")
    public String aboutStaff() {
        return "about/staff";
    }

    @GetMapping("/worship/schedule")
    public String worshipSchedule() {
        return "worship/schedule";
    }

    @GetMapping("/worship/department")
    public String worshipDepartment() {
        return "worship/department";
    }

    @GetMapping("/notice/list")
    public String noticeList(@RequestParam(defaultValue = "0") int page, Model model) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        model.addAttribute("notices", noticeService.getNoticesPaged(pageable));
        return "notice/list";
    }

    @GetMapping("/ministry")
    public String ministryPage(Model model) {
        model.addAttribute("photos", ministryPhotoService.getActivePhotos());
        return "ministry/index";
    }

    @GetMapping("/notice/detail/{id}")
    public String noticeDetail(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeService.getNoticeById(id));
        model.addAttribute("prevNotice", noticeService.getPrevNotice(id));
        model.addAttribute("nextNotice", noticeService.getNextNotice(id));
        return "notice/detail";
    }

    @GetMapping("/location")
    public String location(Model model) {
        locationService.getActiveLocation()
                .ifPresent(loc -> model.addAttribute("loc", loc));
        model.addAttribute("kakaoMapApiKey", kakaoMapApiKey);
        return "location";
    }

    @GetMapping("/sermon")
    public String sermonList(
            @RequestParam(defaultValue = "") String biblePassage,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("sermonDate").descending());
        model.addAttribute("sermons", sermonService.getSermons(biblePassage, pageable));
        model.addAttribute("biblePassage", biblePassage);
        return "sermon/list";
    }

    @GetMapping("/sermon/{id}")
    public String sermonDetail(@PathVariable Long id, Model model) {
        model.addAttribute("sermon", sermonService.getById(id));
        return "sermon/detail";
    }
}
