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

/**
 * 애플리케이션 시작 시 필수 초기 데이터 삽입
 * - latitude / longitude 컬럼 NULL 허용으로 변경 (DB가 NOT NULL로 남아있는 경우 대비)
 * - location 테이블이 비어 있으면 기본 교회 정보 삽입
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final LocationRepository locationRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // 1. latitude / longitude 컬럼을 NULL 허용으로 변경 (이미 NULL 허용이면 오류 무시)
        try {
            jdbcTemplate.execute(
                "ALTER TABLE location MODIFY COLUMN latitude DOUBLE NULL"
            );
            jdbcTemplate.execute(
                "ALTER TABLE location MODIFY COLUMN longitude DOUBLE NULL"
            );
            log.info("[DataInitializer] latitude/longitude 컬럼 NULL 허용으로 변경 완료");
        } catch (Exception e) {
            log.info("[DataInitializer] 컬럼 변경 불필요 또는 이미 적용됨: {}", e.getMessage());
        }

        // 2. location 데이터가 없으면 기본값 삽입
        try {
            long count = locationRepository.count();
            log.info("[DataInitializer] location 테이블 현재 행 수: {}", count);

            if (count == 0) {
                Location loc = new Location();
                loc.setChurchName("샘물교회");
                loc.setAddress("서울특별시 강동구 양재대로 1371 4층 406호");
                loc.setPhone("0507-1377-3927");
                loc.setSubwayInfo("5호선 둔촌동역 4번 출구 도보 2분");
                loc.setBusInfo("3214, 3316, 3412, 3413, 강동01");
                loc.setCarInfo("올림픽대로 → 길동IC → 양재대로 진입 후 둔촌동역 방향");
                loc.setParkingInfo("건물 내 주차장 이용 가능");
                loc.setLatitude(37.534969);   // 둔촌동역 인근 좌표 (fallback)
                loc.setLongitude(127.145398);
                loc.setIsActive(true);

                Location saved = locationRepository.save(loc);
                log.info("[DataInitializer] location 기본 데이터 삽입 완료, id={}", saved.getId());
            } else {
                log.info("[DataInitializer] location 데이터 이미 존재({} 건) — 초기화 건너뜀", count);
            }
        } catch (Exception e) {
            log.error("[DataInitializer] 초기 데이터 삽입 실패: {}", e.getMessage(), e);
        }
    }
}
