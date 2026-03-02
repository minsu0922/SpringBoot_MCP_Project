package com.spring.public_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 일반 사용자용 메인 컨트롤러
 * 홈, 소개, 예배안내, 공지사항, 오시는길 등 공개 페이지 라우팅 처리
 */
@Controller
public class MainController {

    /**
     * 홈 페이지
     * 웹사이트의 메인 페이지를 반환
     */
    @GetMapping("/")
    public String index() {
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
     * 공지사항 목록 페이지
     * 교회의 공지사항 목록을 보여주는 페이지를 반환
     */
    @GetMapping("/notice/list")
    public String noticeList() {
        return "notice/list";
    }

    /**
     * 공지사항 상세 페이지
     * 선택한 공지사항의 상세 내용을 보여주는 페이지를 반환
     */
    @GetMapping("/notice/detail")
    public String noticeDetail() {
        return "notice/detail";
    }

    /**
     * 오시는 길 페이지
     * 교회 위치와 찾아오는 방법을 안내하는 페이지를 반환
     */
    @GetMapping("/location")
    public String location() {
        return "location";
    }
}
