package com.church.website.repository;

import com.church.website.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 교회 위치·연락처 정보 데이터 접근 Repository.
 *
 * <p>JpaRepository 기본 CRUD 외에 단일 활성 레코드 조회 메서드를 제공한다.
 * 교회 정보는 isActive=true 인 레코드 하나만 사용하는 단일 레코드 관리 방식이다.
 * id DESC 정렬로 가장 최근에 저장된 활성 레코드를 가져온다.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * 가장 최근에 저장된 활성 교회 정보를 반환한다.
     * 오시는길 페이지와 메인 페이지 하단에서 사용한다.
     *
     * @return 활성 Location Optional — 등록된 정보가 없으면 Optional.empty()
     */
    Optional<Location> findFirstByIsActiveTrueOrderByIdDesc();
}
