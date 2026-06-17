package com.church.website.service;

import com.church.website.entity.ChurchInfo;
import com.church.website.repository.ChurchInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChurchInfoService {

    private final ChurchInfoRepository churchInfoRepository;

    @Transactional(readOnly = true)
    public Optional<ChurchInfo> getActive() {
        return churchInfoRepository.findFirstByIsActiveTrueOrderByIdDesc();
    }

    @Transactional
    public ChurchInfo save(ChurchInfo updated) {
        ChurchInfo info = churchInfoRepository.findFirstByIsActiveTrueOrderByIdDesc()
                .orElse(new ChurchInfo());
        info.setVision(updated.getVision());
        info.setMission(updated.getMission());
        info.setHistory(updated.getHistory());
        info.setIsActive(true);
        return churchInfoRepository.save(info);
    }
}
