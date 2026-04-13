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

    const NAV_HEIGHT = 72; // 고정 네비 높이

    function goTo(index) {
        if (index < 0 || index >= sections.length) return;
        if (isScrolling) return;

        isScrolling = true;
        current = index;

        const target = sections[index];
        // 섹션 0(hero)은 이미 margin-top: 72px으로 밀려있으므로 네비 보정 불필요
        // 섹션 1~5는 offsetTop 그대로 이동하면 네비(72px)에 가려지므로 빼줌
        const targetY = index === 0 ? 0 : target.offsetTop - NAV_HEIGHT;

        window.scrollTo({ top: targetY, behavior: 'smooth' });

        setTimeout(() => { isScrolling = false; }, DURATION);
    }

    /* 현재 뷰포트에 가장 가까운 섹션 인덱스 계산 */
    function getNearestSection() {
        // 네비 아래쪽 기준점으로 가장 가까운 섹션을 찾음
        const viewTop = window.scrollY + NAV_HEIGHT;
        let nearest = 0;
        let minDist = Infinity;
        sections.forEach((sec, i) => {
            const dist = Math.abs(viewTop - sec.offsetTop);
            if (dist < minDist) { minDist = dist; nearest = i; }
        });
        return nearest;
    }

    /* 휠 이벤트 */
    let lastWheelTime = 0;
    const WHEEL_COOLDOWN = 900; // DURATION보다 약간 길게

    window.addEventListener('wheel', function (e) {
        // 팝업이 열려 있으면 스크롤 무시
        const popup = document.getElementById('popupContainer');
        if (popup && popup.classList.contains('show')) return;

        e.preventDefault();

        if (isScrolling) return; // 이동 중이면 입력 완전 차단

        const now = Date.now();
        if (now - lastWheelTime < WHEEL_COOLDOWN) return; // 쿨다운 중이면 무시

        if (Math.abs(e.deltaY) < 20) return; // 너무 약한 입력 무시

        lastWheelTime = now;
        const dir = e.deltaY > 0 ? 1 : -1;
        current = getNearestSection();
        goTo(current + dir);
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
            const targetY = current === 0 ? 0 : sections[current].offsetTop - NAV_HEIGHT;
            window.scrollTo({ top: targetY, behavior: 'instant' });
        }
    });
})();
