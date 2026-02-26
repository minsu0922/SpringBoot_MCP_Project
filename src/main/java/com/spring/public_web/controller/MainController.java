package com.spring.public_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 소개 페이지
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

    // 예배 안내 페이지
    @GetMapping("/worship/schedule")
    public String worshipSchedule() {
        return "worship/schedule";
    }

    @GetMapping("/worship/department")
    public String worshipDepartment() {
        return "worship/department";
    }

    // 공지사항 페이지
    @GetMapping("/notice/list")
    public String noticeList() {
        return "notice/list";
    }

    @GetMapping("/notice/detail")
    public String noticeDetail() {
        return "notice/detail";
    }

    // 오시는 길 페이지
    @GetMapping("/location")
    public String location() {
        return "location";
    }
}
