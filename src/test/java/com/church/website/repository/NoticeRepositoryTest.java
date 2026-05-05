package com.church.website.repository;

import com.church.website.entity.Notice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("NoticeRepository QueryDSL 검색 테스트")
class NoticeRepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @BeforeEach
    void setUp() {
        noticeRepository.deleteAll();

        noticeRepository.save(Notice.builder()
                .title("주일 예배 안내")
                .content("예배 시간이 변경됩니다.")
                .author("admin")
                .popup(false)
                .build());

        noticeRepository.save(Notice.builder()
                .title("수요 기도회 공지")
                .content("이번 주 수요 기도회 안내입니다.")
                .author("admin")
                .popup(false)
                .build());

        noticeRepository.save(Notice.builder()
                .title("새가족 환영회 일정")
                .content("새가족 환영 행사를 진행합니다.")
                .author("admin")
                .popup(true)
                .popupStartDate(LocalDateTime.now().minusDays(1))
                .popupEndDate(LocalDateTime.now().plusDays(7))
                .build());
    }

    @Test
    @DisplayName("키워드·필터 없이 조회하면 전체를 반환한다")
    void emptyConditionsReturnAll() {
        List<Notice> result = noticeRepository.searchNotices("", "");
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("null 조건도 전체를 반환한다")
    void nullConditionsReturnAll() {
        List<Notice> result = noticeRepository.searchNotices(null, null);
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("제목 키워드로 검색한다")
    void searchByTitleKeyword() {
        List<Notice> result = noticeRepository.searchNotices("예배", "");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("주일 예배 안내");
    }

    @Test
    @DisplayName("키워드는 제목에 포함(contains)으로 검색한다")
    void keywordIsContainsSearch() {
        List<Notice> result = noticeRepository.searchNotices("공지", "");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("공지");
    }

    @Test
    @DisplayName("팝업 ON 필터는 팝업 활성 공지만 반환한다")
    void filterPopupOn() {
        List<Notice> result = noticeRepository.searchNotices("", "on");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isPopup()).isTrue();
    }

    @Test
    @DisplayName("팝업 OFF 필터는 팝업 비활성 공지만 반환한다")
    void filterPopupOff() {
        List<Notice> result = noticeRepository.searchNotices("", "off");
        assertThat(result).hasSize(2);
        result.forEach(n -> assertThat(n.isPopup()).isFalse());
    }

    @Test
    @DisplayName("키워드와 팝업 필터를 동시에 적용한다")
    void combineKeywordAndPopupFilter() {
        List<Notice> result = noticeRepository.searchNotices("새가족", "on");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("새가족");
        assertThat(result.get(0).isPopup()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 키워드는 빈 결과를 반환한다")
    void noMatchReturnsEmpty() {
        List<Notice> result = noticeRepository.searchNotices("존재하지않는키워드XYZ", "");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("결과는 생성일 내림차순으로 정렬된다")
    void resultIsOrderedByCreatedAtDesc() {
        List<Notice> result = noticeRepository.searchNotices("", "");

        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getCreatedAt())
                    .isAfterOrEqualTo(result.get(i + 1).getCreatedAt());
        }
    }
}
