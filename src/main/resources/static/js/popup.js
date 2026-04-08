/**
 * 샘물교회 메인 팝업 관리 스크립트
 * - 기간 설정된 팝업 공지사항 노출
 * - 다중 팝업 슬라이드 지원
 * - 하루 동안 보지 않기 (localStorage)
 * - ESC / 오버레이 클릭으로 닫기
 */

const POPUP_HIDE_KEY = 'churchPopupHideUntil';

document.addEventListener('DOMContentLoaded', function () {

    const overlay    = document.getElementById('popupOverlay');
    const container  = document.getElementById('popupContainer');
    const closeBtn   = document.getElementById('popupClose');
    const confirmBtn = document.getElementById('popupConfirm');
    const prevBtn    = document.getElementById('popupPrev');
    const nextBtn    = document.getElementById('popupNext');
    const pagerEl    = document.getElementById('popupPager');

    // 팝업 요소 없으면 종료 (활성 팝업 없음)
    if (!overlay || !container) return;

    const slides = Array.from(container.querySelectorAll('.popup-slide'));
    const total  = slides.length;
    let current  = 0;

    // ── 슬라이드 전환 ──
    function goTo(index) {
        slides[current].classList.remove('active');
        current = (index + total) % total;
        slides[current].classList.add('active');
        if (pagerEl) {
            pagerEl.innerHTML = (current + 1) + ' / ' + total;
        }
    }

    // 첫 슬라이드 활성화
    if (slides.length > 0) slides[0].classList.add('active');

    if (prevBtn) prevBtn.addEventListener('click', function () { goTo(current - 1); });
    if (nextBtn) nextBtn.addEventListener('click', function () { goTo(current + 1); });

    // ── 숨김 기한 확인 후 팝업 표시 ──
    const hideUntil = localStorage.getItem(POPUP_HIDE_KEY);
    const now = Date.now();
    if (!hideUntil || now > parseInt(hideUntil)) {
        showPopup();
    }

    // ── 이벤트 바인딩 ──
    if (closeBtn) closeBtn.addEventListener('click', hidePopup);

    if (confirmBtn) {
        confirmBtn.addEventListener('click', function () {
            const cb = document.getElementById('hideForDay');
            if (cb && cb.checked) setHideForDay();
            hidePopup();
        });
    }

    overlay.addEventListener('click', hidePopup);

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && container.classList.contains('show')) hidePopup();
        if (e.key === 'ArrowLeft'  && total > 1) goTo(current - 1);
        if (e.key === 'ArrowRight' && total > 1) goTo(current + 1);
    });
});

function showPopup() {
    const overlay   = document.getElementById('popupOverlay');
    const container = document.getElementById('popupContainer');
    if (!overlay || !container) return;
    setTimeout(function () {
        overlay.classList.add('show');
        container.classList.add('show');
    }, 300);
    document.body.style.overflow = 'hidden';
}

function hidePopup() {
    const overlay   = document.getElementById('popupOverlay');
    const container = document.getElementById('popupContainer');
    if (!overlay || !container) return;
    overlay.classList.remove('show');
    container.classList.remove('show');
    document.body.style.overflow = '';
}

function setHideForDay() {
    const until = Date.now() + 24 * 60 * 60 * 1000;
    localStorage.setItem(POPUP_HIDE_KEY, until.toString());
}
