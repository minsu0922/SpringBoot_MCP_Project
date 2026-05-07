package com.church.website.service;

import com.church.website.entity.Location;
import com.church.website.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 교회 위치·연락처 정보 비즈니스 로직 서비스.
 *
 * <p>교회 정보는 단일 레코드(isActive=true)로 관리한다.
 * save() 메서드는 기존 활성 레코드를 찾아 업데이트하거나 없으면 신규 생성하는
 * upsert(update or insert) 방식을 사용한다.
 *
 * <p>부분 업데이트 방어 처리:
 * churchName, address는 폼에서 빈 값으로 전송해도 기존 값이 유지되도록 null/blank 체크를 한다.
 * 실수로 중요한 필드를 비워 저장하더라도 기존 데이터가 지워지지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    /**
     * 현재 활성 교회 정보를 반환한다.
     *
     * @return 활성 Location Optional — 등록된 위치 정보가 없으면 Optional.empty()
     */
    @Transactional(readOnly = true)
    public Optional<Location> getActiveLocation() {
        return locationRepository.findFirstByIsActiveTrueOrderByIdDesc();
    }

    /**
     * 교회 기본 정보 저장 (upsert 방식).
     *
     * <p>기존 활성 레코드를 찾아 업데이트하고, 없으면 새로 생성한다.
     * churchName과 address는 빈 값이 들어오면 기존 값을 그대로 유지한다.
     * (관리자가 실수로 핵심 정보를 비워 저장해도 데이터가 유지됨)
     *
     * @param updated 폼에서 입력한 새 위치 정보
     * @return 저장된 Location 엔티티
     */
    @Transactional
    public Location save(Location updated) {
        Location location = locationRepository.findFirstByIsActiveTrueOrderByIdDesc()
                .orElse(new Location());

        // 빈 값으로 덮어쓰지 않도록 방어 — 입력이 있을 때만 업데이트
        if (updated.getChurchName() != null && !updated.getChurchName().isBlank())
            location.setChurchName(updated.getChurchName());
        if (updated.getAddress() != null && !updated.getAddress().isBlank())
            location.setAddress(updated.getAddress());

        location.setLatitude(updated.getLatitude());
        location.setLongitude(updated.getLongitude());
        location.setPhone(updated.getPhone());
        location.setFax(updated.getFax());
        location.setEmail(updated.getEmail());
        location.setSubwayInfo(updated.getSubwayInfo());
        location.setBusInfo(updated.getBusInfo());
        location.setCarInfo(updated.getCarInfo());
        location.setParkingInfo(updated.getParkingInfo());
        location.setIsActive(true);

        log.info("[LocationService] 교회 정보 저장: {}", location.getChurchName());
        return locationRepository.save(location);
    }
}
