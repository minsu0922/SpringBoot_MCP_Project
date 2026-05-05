package com.church.website.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Spring Security 접근 제어 테스트")
class SecurityAccessTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ──────────────────────────────────────────────
    // 공개 페이지 (인증 불필요)
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("공개 페이지는 인증 없이 접근 가능하다")
    class PublicPages {

        @Test
        @DisplayName("홈 페이지 접근")
        void homePage() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("공지사항 목록 접근")
        void noticeList() throws Exception {
            mockMvc.perform(get("/notice/list"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("새가족 등록 폼 접근")
        void newFamilyForm() throws Exception {
            mockMvc.perform(get("/new-family"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("사역소개 페이지 접근")
        void ministryPage() throws Exception {
            mockMvc.perform(get("/ministry"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("설교 목록 접근")
        void sermonList() throws Exception {
            mockMvc.perform(get("/sermon"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("오시는 길 접근")
        void locationPage() throws Exception {
            mockMvc.perform(get("/location"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("로그인 페이지 접근")
        void loginPage() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────
    // 관리자 페이지 — 비인증 접근 차단
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("관리자 페이지는 비인증 접근 시 로그인으로 리다이렉트된다")
    class AdminUnauthenticated {

        @Test
        @DisplayName("/admin 대시보드")
        void adminDashboard() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @DisplayName("/admin/notices")
        void adminNotices() throws Exception {
            mockMvc.perform(get("/admin/notices"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @DisplayName("/admin/new-family")
        void adminNewFamily() throws Exception {
            mockMvc.perform(get("/admin/new-family"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        @Test
        @DisplayName("/admin/settings")
        void adminSettings() throws Exception {
            mockMvc.perform(get("/admin/settings"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    // ──────────────────────────────────────────────
    // 관리자 페이지 — ROLE_ADMIN 접근 허용
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("ROLE_ADMIN 사용자는 관리자 페이지에 접근할 수 있다")
    class AdminAuthenticated {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("/admin 대시보드 접근 성공")
        void adminDashboard() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("/admin/notices 접근 성공")
        void adminNotices() throws Exception {
            mockMvc.perform(get("/admin/notices"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("/admin/new-family 접근 성공")
        void adminNewFamily() throws Exception {
            mockMvc.perform(get("/admin/new-family"))
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────
    // ROLE_ADMIN 아닌 권한은 403
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("ROLE_ADMIN 이 아닌 인증 사용자는 관리자 페이지에서 403을 받는다")
    class AdminForbidden {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("ROLE_USER로 /admin 접근 시 Forbidden")
        void userRoleForbidden() throws Exception {
            mockMvc.perform(get("/admin"))
                    .andExpect(status().isForbidden());
        }
    }

    // ──────────────────────────────────────────────
    // CSRF 보호
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("POST 요청에 CSRF 보호가 적용된다")
    class CsrfProtection {

        @Test
        @DisplayName("CSRF 토큰 없이 새가족 등록 POST 시 403")
        void newFamilySubmitWithoutCsrf() throws Exception {
            mockMvc.perform(post("/new-family/submit")
                            .param("name", "홍길동")
                            .param("phone", "010-1234-5678"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CSRF 토큰 포함 시 정상 처리된다")
        void newFamilySubmitWithCsrf() throws Exception {
            mockMvc.perform(post("/new-family/submit")
                            .with(csrf())
                            .param("name", "홍길동")
                            .param("phone", "010-1234-5678"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/new-family/complete"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("CSRF 토큰 없이 관리자 POST 시 403")
        void adminPostWithoutCsrf() throws Exception {
            mockMvc.perform(post("/admin/notices/delete/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
