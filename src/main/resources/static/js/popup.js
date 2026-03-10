/**
 * 메인 페이지 팝업 관리 스크립트
 * - 페이지 로드 시 팝업 자동 표시
 * - 하루 동안 보지 않기 기능 (localStorage 사용)
 * - 팝업 닫기 및 오버레이 클릭 이벤트 처리
 */

// 로컬 스토리지 키
const POPUP_HIDE_KEY = 'churchPopupHideUntil';

/**
 * 페이지 로드 시 팝업 표시 여부 확인
 */
document.addEventListener('DOMContentLoaded', function() {
    // 팝업 숨김 기한 확인
    const hideUntil = localStorage.getItem(POPUP_HIDE_KEY);
    const now = new Date().getTime();
    
    // 숨김 기한이 없거나 만료된 경우 팝업 표시
    if (!hideUntil || now > parseInt(hideUntil)) {
        showPopup();
    }
});

/**
 * 팝업 표시
 */
function showPopup() {
    const overlay = document.getElementById('popupOverlay');
    const container = document.getElementById('popupContainer');
    
    // 애니메이션을 위한 짧은 딜레이
    setTimeout(() => {
        overlay.classList.add('show');
        container.classList.add('show');
    }, 100);
    
    // body 스크롤 방지
    document.body.style.overflow = 'hidden';
}

/**
 * 팝업 닫기
 */
function hidePopup() {
    const overlay = document.getElementById('popupOverlay');
    const container = document.getElementById('popupContainer');
    
    overlay.classList.remove('show');
    container.classList.remove('show');
    
    // body 스크롤 복원
    document.body.style.overflow = 'auto';
}

/**
 * 하루 동안 보지 않기 설정
 */
function setHideForDay() {
    const now = new Date().getTime();
    const oneDay = 24 * 60 * 60 * 1000; // 24시간을 밀리초로 변환
    const hideUntil = now + oneDay;
    
    localStorage.setItem(POPUP_HIDE_KEY, hideUntil.toString());
}

// 닫기 버튼 클릭 이벤트
document.getElementById('popupClose').addEventListener('click', function() {
    hidePopup();
});

// 확인 버튼 클릭 이벤트
document.getElementById('popupConfirm').addEventListener('click', function() {
    const hideCheckbox = document.getElementById('hideForDay');
    
    // 하루 동안 보지 않기 체크박스 확인
    if (hideCheckbox.checked) {
        setHideForDay();
    }
    
    hidePopup();
});

// 오버레이 클릭 시 팝업 닫기
document.getElementById('popupOverlay').addEventListener('click', function() {
    hidePopup();
});

// ESC 키로 팝업 닫기
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        const container = document.getElementById('popupContainer');
        if (container.classList.contains('show')) {
            hidePopup();
        }
    }
});
