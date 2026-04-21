package com.church.website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 애플리케이션 메인 클래스
 * 교회 웹사이트 애플리케이션의 진입점으로 애플리케이션을 시작
 */
@SpringBootApplication

public class ChurchWebsiteApplication {

    /**
     * 애플리케이션 시작 메서드
     * Spring Boot 애플리케이션을 실행하고 웹 서버를 구동
     */
    public static void main(String[] args) {
        SpringApplication.run(ChurchWebsiteApplication.class, args);
    }

}


