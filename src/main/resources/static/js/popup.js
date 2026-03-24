/**
 * 메인 페이지 팝업 관리 스크립트
 * - 페이지 로드 시 팝업 자동 표시
 * - 하루 동안 보지 않기 기능 (localStorage 사용)
 * - 팝업 닫기 및 오버레이 클릭 이벤트 처리
 */

const POPUP_HIDE_KEY = 'churchPopupHideUntil';

document.addEventListener('DOMContentLoaded', function() {
    // 팝업 요소가 존재하는지 먼저 확인 (팝업 공지사항이 없으면 요소 자체가 없음)
    const overlay = document.getElementById('popupOverlay');
    const container = document.getElementById('popupContainer');
    const closeBtn = document.getElementById('popupClose');
    const confirmBtn = document.getElementById('popupConfirm');

    if (!overlay || !container) {
        return; // 활성 팝업 공지사항 없음, 아무것도 하지 않음
    }

    // 숨김 기한 확인 후 팝업 표시
    const hideUntil = localStorage.getItem(POPUP_HIDE_KEY);
    const now = new Date().getTime();
    if (!hideUntil || now > parseInt(hideUntil)) {
        showPopup();
    }

    // 닫기 버튼
    if (closeBtn) {
        closeBtn.addEventListener('click', hidePopup);
    }

    // 확인 버튼
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function() {
            const hideCheckbox = document.getElementById('hideForDay');
            if (hideCheckbox && hideCheckbox.checked) {
                setHideForDay();
            }
            hidePopup();
        });
    }

    // 오버레이 클릭
    overlay.addEventListener('click', hidePopup);

    // ESC 키
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && container.classList.contains('show')) {
            hidePopup();
        }
    });
});

function showPopup() {
    const overlay = document.getElementById('popupOverlay');
    const container = document.getElementById('popupContainer');
    if (!overlay || !container) return;
    setTimeout(() => {
        overlay.classList.add('show');
        container.classList.add('show');
    }, 100);
    document.body.style.overflow = 'hidden';
}

function hidePopup() {
    const overlay = document.getElementById('popupOverlay');
    const container = document.getElementById('popupContainer');
    if (!overlay || !container) return;
    overlay.classList.remove('show');
    container.classList.remove('show');
    document.body.style.overflow = 'auto';
}

function setHideForDay() {
    const hideUntil = new Date().getTime() + (24 * 60 * 60 * 1000);
    localStorage.setItem(POPUP_HIDE_KEY, hideUntil.toString());
}
