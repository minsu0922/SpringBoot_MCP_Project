package com.church.website.config;

import com.church.website.entity.Location;
import com.church.website.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final LocationRepository locationRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        alterLocationColumns();
        insertDefaultLocationIfEmpty();
    }

    /**
     * latitude, longitude 컬럼을 NULL 허용으로 변경.
     *
     * JPA ddl-auto=update는 컬럼을 추가할 수 있지만,
     * 기존 NOT NULL 컬럼의 제약 조건을 변경하는 것은 지원하지 않음.
     * 따라서 애플리케이션 시작 시 직접 JDBC로 DDL을 실행해 보정.
     * 이미 NULL 허용인 경우 MySQL이 오류를 반환하므로 예외는 무시.
     */
    private void alterLocationColumns() {
        try {
            jdbcTemplate.execute("ALTER TABLE location MODIFY COLUMN latitude DOUBLE NULL");
            jdbcTemplate.execute("ALTER TABLE location MODIFY COLUMN longitude DOUBLE NULL");
            log.info("[DataInitializer] latitude/longitude 컬럼 NULL 허용으로 변경 완료");
        } catch (Exception e) {
            log.info("[DataInitializer] 컬럼 변경 불필요 또는 이미 적용됨: {}", e.getMessage());
        }
    }

    /**
     * location 테이블이 비어있을 때 샘물교회 기본 정보를 삽입.
     * 최초 배포 시 관리자가 설정 페이지를 열기 전에도 지도가 표시되도록 하기 위한 fallback 데이터.
     */
    private void insertDefaultLocationIfEmpty() {
        try {
            if (locationRepository.count() > 0) {
                log.info("[DataInitializer] location 데이터 이미 존재 — 초기화 건너뜀");
                return;
            }
            Location loc = new Location();
            loc.setChurchName("샘물교회");
            loc.setAddress("서울특별시 강동구 양재대로 1371 4층 406호");
            loc.setPhone("0507-1377-3927");
            loc.setSubwayInfo("5호선 둔촌동역 4번 출구 도보 2분");
            loc.setBusInfo("3214, 3316, 3412, 3413, 강동01");
            loc.setCarInfo("올림픽대로 → 길동IC → 양재대로 진입 후 둔촌동역 방향");
            loc.setParkingInfo("건물 내 주차장 이용 가능");
            loc.setLatitude(37.534969);
            loc.setLongitude(127.145398);
            loc.setIsActive(true);
            Location saved = locationRepository.save(loc);
            log.info("[DataInitializer] location 기본 데이터 삽입 완료, id={}", saved.getId());
        } catch (Exception e) {
            log.error("[DataInitializer] 초기 데이터 삽입 실패: {}", e.getMessage(), e);
        }
    }
}
