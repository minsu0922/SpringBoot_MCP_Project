package com.church.website.config;

import com.church.website.controller.MainController;
import com.church.website.entity.Location;
import com.church.website.entity.WorshipSchedule;
import com.church.website.service.LocationService;
import com.church.website.service.WorshipScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 공개 페이지 공통 footer에 필요한 데이터를 모든 {@link MainController} 핸들러 모델에 주입한다.
 *
 * <p>footer는 12개 공개 페이지가 공유하는 프래그먼트(fragments/public.html :: siteFooter)이며,
 * 전화·주소·주일예배 시간을 표시한다. 각 컨트롤러 메서드마다 중복으로 모델에 담지 않도록
 * 여기서 한 곳에서 주입한다.
 *
 * <p>{@code assignableTypes = MainController.class}로 범위를 한정해 관리자 페이지 등에는
 * 불필요한 조회가 발생하지 않도록 한다. 값이 없으면 null을 담아 템플릿의 기본값(fallback)이
 * 사용되도록 한다.
 */
@ControllerAdvice(assignableTypes = MainController.class)
@RequiredArgsConstructor
public class PublicFooterAdvice {

    private final LocationService locationService;
    private final WorshipScheduleService worshipScheduleService;

    /** footer·오시는길에서 사용하는 교회 위치/연락처 정보 (없으면 null). */
    @ModelAttribute("loc")
    public Location loc() {
        return locationService.getActiveLocation().orElse(null);
    }

    /** footer에 대표로 노출할 주일예배 1건 (없으면 null). */
    @ModelAttribute("footerWorship")
    public WorshipSchedule footerWorship() {
        return worshipScheduleService.getPrimarySunday().orElse(null);
    }
}
