/**
 * 섹션 단위 스크롤 (풀페이지 스크롤)
 * 마우스 휠, 터치 스와이프, 키보드 방향키로 섹션 간 이동
 */
(function () {
    const sections = Array.from(document.querySelectorAll('[data-section]'))
        .sort((a, b) => +a.dataset.section - +b.dataset.section);

    if (sections.length === 0) return;

    let current = 0;
    let isScrolling = false;
    const DURATION = 800; // ms

    function goTo(index) {
        if (index < 0 || index >= sections.length) return;
        if (isScrolling) return;

        isScrolling = true;
        current = index;

        const target = sections[index];
        const targetY = target.getBoundingClientRect().top + window.scrollY;

        window.scrollTo({ top: targetY, behavior: 'smooth' });

        setTimeout(() => { isScrolling = false; }, DURATION);
    }

    /* 현재 뷰포트에 가장 가까운 섹션 인덱스 계산 */
    function getNearestSection() {
        const scrollY = window.scrollY + window.innerHeight / 2;
        let nearest = 0;
        let minDist = Infinity;
        sections.forEach((sec, i) => {
            const secMid = sec.offsetTop + sec.offsetHeight / 2;
            const dist = Math.abs(scrollY - secMid);
            if (dist < minDist) { minDist = dist; nearest = i; }
        });
        return nearest;
    }

    /* 휠 이벤트 */
    let wheelAccum = 0;
    const WHEEL_THRESHOLD = 50;

    window.addEventListener('wheel', function (e) {
        // 팝업이 열려 있으면 스크롤 무시
        const popup = document.getElementById('popupContainer');
        if (popup && popup.classList.contains('show')) return;

        e.preventDefault();

        wheelAccum += e.deltaY;

        if (Math.abs(wheelAccum) >= WHEEL_THRESHOLD) {
            const dir = wheelAccum > 0 ? 1 : -1;
            wheelAccum = 0;
            current = getNearestSection();
            goTo(current + dir);
        }
    }, { passive: false });

    /* 터치 스와이프 */
    let touchStartY = 0;

    window.addEventListener('touchstart', function (e) {
        touchStartY = e.touches[0].clientY;
    }, { passive: true });

    window.addEventListener('touchend', function (e) {
        const diff = touchStartY - e.changedTouches[0].clientY;
        if (Math.abs(diff) < 40) return;
        const dir = diff > 0 ? 1 : -1;
        current = getNearestSection();
        goTo(current + dir);
    }, { passive: true });

    /* 키보드 */
    window.addEventListener('keydown', function (e) {
        if (['ArrowDown', 'PageDown'].includes(e.key)) {
            e.preventDefault();
            current = getNearestSection();
            goTo(current + 1);
        } else if (['ArrowUp', 'PageUp'].includes(e.key)) {
            e.preventDefault();
            current = getNearestSection();
            goTo(current - 1);
        }
    });

    /* 리사이즈 시 현재 섹션 위치 재조정 */
    window.addEventListener('resize', function () {
        if (!isScrolling) {
            current = getNearestSection();
            const target = sections[current];
            window.scrollTo({ top: target.offsetTop, behavior: 'instant' });
        }
    });
})();
