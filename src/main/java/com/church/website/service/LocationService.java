package com.church.website.service;

import com.church.website.entity.Location;
import com.church.website.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 교회 기본 정보(위치) 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    /** 활성화된 교회 정보 조회 */
    public Optional<Location> getActiveLocation() {
        return locationRepository.findFirstByIsActiveTrueOrderByIdDesc();
    }

    /**
     * 교회 정보 저장 (단일 레코드 upsert)
     * 기존 레코드가 있으면 업데이트, 없으면 신규 생성
     */
    @Transactional
    public Location save(Location updated) {
        Location location = locationRepository.findFirstByIsActiveTrueOrderByIdDesc()
                .orElse(new Location());
        location.setChurchName(updated.getChurchName());
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
        log.info("교회 정보 저장: {}", location.getChurchName());
        return locationRepository.save(location);
    }
}
