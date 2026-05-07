package com.church.website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 샘물교회 웹사이트 Spring Boot 애플리케이션 진입점.
 *
 * <p>@SpringBootApplication 하나로 세 가지가 동시에 활성화된다.
 * <ul>
 *   <li>@Configuration        : 이 클래스를 Bean 설정 소스로 사용</li>
 *   <li>@ComponentScan        : com.church.website 하위 패키지를 스캔해 @Controller,
 *       @Service, @Repository 등 Bean 자동 등록</li>
 *   <li>@EnableAutoConfiguration : classpath에 추가된 의존성(JPA, Security 등)을 보고
 *       필요한 설정을 자동으로 구성</li>
 * </ul>
 */
@SpringBootApplication
public class ChurchWebsiteApplication {

    /**
     * JVM이 프로그램 시작 시 최초로 호출하는 메서드.
     * SpringApplication.run()이 내장 Tomcat 서버를 구동하고 모든 Bean을 초기화하여
     * localhost:8080 으로 HTTP 요청을 받을 준비를 마친다.
     */
    public static void main(String[] args) {
        SpringApplication.run(ChurchWebsiteApplication.class, args);
    }
}
