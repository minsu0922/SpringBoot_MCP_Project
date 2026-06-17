package com.church.website.controller;

import com.church.website.entity.MinistryPhoto;
import com.church.website.service.ChurchInfoService;
import com.church.website.service.DepartmentService;
import com.church.website.service.LocationService;
import com.church.website.service.MainImageService;
import com.church.website.service.MinistryPhotoService;
import com.church.website.service.NoticeService;
import com.church.website.service.PastorService;
import com.church.website.service.SermonService;
import com.church.website.service.StaffMemberService;
import com.church.website.service.WorshipScheduleService;
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

/**
 * 방문자용 공개 페이지 전체를 담당하는 컨트롤러.
 *
 * <p>로그인 없이 누구나 접근할 수 있는 모든 페이지의 URL 라우팅을 처리한다.
 * SecurityConfig에서 이 경로들은 .permitAll()로 설정되어 있다.
 *
 * <p>제공 페이지:
 * <ul>
 *   <li>/              : 메인 홈 — 슬라이더·최신 공지·최신 설교·오시는길·말씀 표시</li>
 *   <li>/about/**      : 교회 소개 (교회·담임목사·교역자)</li>
 *   <li>/worship/**    : 예배 안내 (예배 시간표·부서 소개)</li>
 *   <li>/notice/**     : 공지사항 목록·상세</li>
 *   <li>/ministry      : 사역 소개 사진 갤러리</li>
 *   <li>/location      : 오시는길 (카카오맵)</li>
 *   <li>/sermon/**     : 설교 영상 목록·상세</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
public class MainController {

    /**
     * 메인 화면에 표시할 오늘의 말씀 후보 목록.
     * 날짜(getDayOfYear())를 인덱스로 사용하여 매일 다른 구절이 표시되도록 한다.
     */
    private static final List<String[]> DAILY_VERSES = List.of(
        new String[]{ "“내가 곧 길이요 진리요 생명이니 나로 말미암지 않고는 아버지께로 올 자가 없느니라”", "요한복음 14:6" },
        new String[]{ "“여호와는 나의 목자시니 내게 부족함이 없으리로다”", "시편 23:1" },
        new String[]{ "“하나님이 세상을 이처럼 사랑하사 독생자를 주셨으니 이는 그를 믿는 자마다 멸망하지 않고 영생을 얻게 하려 하심이라”", "요한복음 3:16" },
        new String[]{ "“내게 능력 주시는 자 안에서 내가 모든 것을 할 수 있느니라”", "빌립보서 4:13" },
        new String[]{ "“너는 마음을 다하여 여호와를 신뢰하고 네 명철을 의지하지 말라”", "잠언 3:5" }
    );

    // application.properties의 kakao.map.api.key 값을 주입받아 뷰에 전달한다
    @Value("${kakao.map.api.key}")
    private String kakaoMapApiKey;

    private final MainImageService mainImageService;
    private final NoticeService noticeService;
    private final MinistryPhotoService ministryPhotoService;
    private final LocationService locationService;
    private final SermonService sermonService;
    private final ChurchInfoService churchInfoService;
    private final PastorService pastorService;
    private final StaffMemberService staffMemberService;
    private final WorshipScheduleService worshipScheduleService;
    private final DepartmentService departmentService;

    /**
     * 메인 홈 페이지.
     * 슬라이더·오늘의 말씀·최신 공지·최신 설교·팝업 공지·사역사진 미리보기·오시는길을 조합한다.
     * 사역사진은 최대 4장만 보여주고, 나머지는 /ministry 페이지에서 확인할 수 있다.
     */
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

    /** 교회 소개 페이지. */
    @GetMapping("/about/church")
    public String aboutChurch(Model model) {
        churchInfoService.getActive().ifPresent(info -> model.addAttribute("churchInfo", info));
        return "about/church";
    }

    /** 담임목사 소개 페이지. */
    @GetMapping("/about/pastor")
    public String aboutPastor(Model model) {
        pastorService.getActive().ifPresent(pastor -> model.addAttribute("pastor", pastor));
        return "about/pastor";
    }

    /** 교역자 소개 페이지. */
    @GetMapping("/about/staff")
    public String aboutStaff(Model model) {
        model.addAttribute("staffList", staffMemberService.getActive());
        return "about/staff";
    }

    /** 예배 시간표 페이지. */
    @GetMapping("/worship/schedule")
    public String worshipSchedule(Model model) {
        model.addAttribute("schedules", worshipScheduleService.getActive());
        return "worship/schedule";
    }

    /** 예배 부서 소개 페이지. */
    @GetMapping("/worship/department")
    public String worshipDepartment(Model model) {
        model.addAttribute("departments", departmentService.getActive());
        return "worship/department";
    }

    /** 공지사항 목록 (페이지네이션). 최신순으로 10개씩 표시한다. */
    @GetMapping("/notice/list")
    public String noticeList(@RequestParam(defaultValue = "0") int page, Model model) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        model.addAttribute("notices", noticeService.getNoticesPaged(pageable));
        return "notice/list";
    }

    /** 사역 소개 사진 갤러리 — 활성화된 사진 전체를 표시 순서대로 보여준다. */
    @GetMapping("/ministry")
    public String ministryPage(Model model) {
        model.addAttribute("photos", ministryPhotoService.getActivePhotos());
        return "ministry/index";
    }

    /**
     * 공지사항 상세 조회.
     * 조회수가 자동으로 1 증가하며, 이전글·다음글 링크도 함께 제공한다.
     */
    @GetMapping("/notice/detail/{id}")
    public String noticeDetail(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticeService.getNoticeById(id));
        model.addAttribute("prevNotice", noticeService.getPrevNotice(id));
        model.addAttribute("nextNotice", noticeService.getNextNotice(id));
        return "notice/detail";
    }

    /** 오시는길 페이지 — 카카오맵 API 키와 교회 위치 정보를 함께 전달한다. */
    @GetMapping("/location")
    public String location(Model model) {
        locationService.getActiveLocation()
                .ifPresent(loc -> model.addAttribute("loc", loc));
        model.addAttribute("kakaoMapApiKey", kakaoMapApiKey);
        return "location";
    }

    /** 설교 영상 목록 (성경 본문 검색·페이지네이션). 설교 날짜 내림차순으로 정렬한다. */
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

    /** 설교 영상 상세 — YouTube iframe으로 영상을 임베드해 보여준다. */
    @GetMapping("/sermon/{id}")
    public String sermonDetail(@PathVariable Long id, Model model) {
        model.addAttribute("sermon", sermonService.getById(id));
        return "sermon/detail";
    }
}
