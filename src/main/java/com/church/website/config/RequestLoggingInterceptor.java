package com.church.website.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String ATTR_START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 요청 시작 시각을 request 속성에 저장해두고 afterCompletion에서 처리시간 계산에 사용
        request.setAttribute(ATTR_START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long duration = System.currentTimeMillis() - (long) request.getAttribute(ATTR_START_TIME);
        String ip = resolveClientIp(request);

        // 예외 발생 요청은 ERROR, 정상 요청은 DEBUG로 분리하여 운영 로그 가독성 확보
        if (ex != null) {
            log.error("[{}] {} {} → {} ({}ms)", ip, request.getMethod(), request.getRequestURI(),
                    response.getStatus(), duration);
        } else {
            log.debug("[{}] {} {} → {} ({}ms)", ip, request.getMethod(), request.getRequestURI(),
                    response.getStatus(), duration);
        }
    }

    /**
     * 실제 클라이언트 IP 추출
     *
     * Nginx 등 리버스 프록시를 거치면 request.getRemoteAddr()은 프록시 서버 IP를 반환.
     * X-Forwarded-For 헤더에 원래 클라이언트 IP가 콤마 구분으로 전달되므로 첫 번째 값을 사용.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
