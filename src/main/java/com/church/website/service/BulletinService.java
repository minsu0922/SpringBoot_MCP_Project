package com.church.website.service;

import com.church.website.entity.Bulletin;
import com.church.website.repository.BulletinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BulletinService {

    private final BulletinRepository bulletinRepository;

    public Page<Bulletin> getAll(Pageable pageable) {
        return bulletinRepository.findAllByOrderByWorshipDateDesc(pageable);
    }

    public Bulletin getById(Long id) {
        return bulletinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주보를 찾을 수 없습니다: " + id));
    }

    public Optional<Bulletin> getLatest() {
        return bulletinRepository.findTopByOrderByWorshipDateDesc();
    }

    public Bulletin save(Bulletin bulletin) {
        return bulletinRepository.save(bulletin);
    }

    public void delete(Long id) {
        bulletinRepository.deleteById(id);
    }
}
