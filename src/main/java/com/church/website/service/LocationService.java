package com.church.website.service;

import com.church.website.entity.Location;
import com.church.website.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    @Transactional(readOnly = true)
    public Optional<Location> getActiveLocation() {
        return locationRepository.findFirstByIsActiveTrueOrderByIdDesc();
    }

    /**
     * 교회 기본 정보 저장 (upsert 방식).
     *
     * 교회 정보는 단일 레코드로 관리: 기존 활성 레코드가 있으면 업데이트, 없으면 신규 생성.
     * churchName, address는 빈 값으로 덮어쓰지 않도록 null/blank 방어 처리.
     * (폼에서 해당 필드를 비워서 전송했을 때 기존 값이 유지되어야 하는 요구사항 반영)
     */
    @Transactional
    public Location save(Location updated) {
        Location location = locationRepository.findFirstByIsActiveTrueOrderByIdDesc()
                .orElse(new Location());

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
