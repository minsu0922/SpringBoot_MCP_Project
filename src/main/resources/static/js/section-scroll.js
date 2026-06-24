/**
 * 섹션 단위 스크롤 (풀페이지 스크롤)
 * 마우스 휠, 터치 스와이프, 키보드 방향키로 섹션 간 이동
 *
 * 한 번의 입력 = 한 슬라이드 이동. 데스크톱/모바일(좁은 화면) 모두 동일하게 동작한다.
 * 단, 콘텐츠가 세로로 쌓여 한 섹션이 화면보다 길 경우(주로 모바일),
 * 섹션 내부에 남은 영역이 있으면 먼저 한 화면씩 페이징한 뒤
 * 다음/이전 섹션으로 넘어간다(콘텐츠 잘림 방지).
 */
(function () {
    const sections = Array.from(document.querySelectorAll('[data-section]'))
        .sort((a, b) => +a.dataset.section - +b.dataset.section);

    if (sections.length === 0) return;

    let current = 0;
    let isScrolling = false;
    const DURATION = 800; // ms

    const NAV_HEIGHT = 72; // 고정 네비 높이
    const EDGE = 4; // px 허용 오차 (섹션 가장자리 판정)

    /* 섹션 i의 정렬 스크롤 위치(상단을 네비 아래에 맞춤) */
    function sectionTop(i) {
        return i === 0 ? 0 : sections[i].offsetTop - NAV_HEIGHT;
    }

    function scrollToY(y) {
        isScrolling = true;
        window.scrollTo({ top: y, behavior: 'smooth' });
        setTimeout(() => { isScrolling = false; }, DURATION);
    }

    function goTo(index) {
        if (index < 0 || index >= sections.length) return;
        if (isScrolling) return;
        current = index;
        scrollToY(sectionTop(index));
    }

    /* 섹션 내부가 화면보다 길면 한 화면씩 페이징, 끝에 닿으면 섹션 이동 */
    function advance(dir) {
        if (isScrolling) return;
        current = getNearestSection();

        const sec = sections[current];
        const winH = window.innerHeight;
        const scrollY = window.scrollY;
        const step = winH - NAV_HEIGHT;
        const secBottomAbs = sec.offsetTop + sec.offsetHeight;
        const viewportBottom = scrollY + winH;
        const topAligned = sectionTop(current);

        if (dir > 0) {
            // 아래로: 섹션 하단이 아직 화면 밖이면 한 화면 내림
            if (viewportBottom < secBottomAbs - EDGE) {
                scrollToY(Math.min(scrollY + step, secBottomAbs - winH));
            } else {
                goTo(current + 1);
            }
        } else {
            // 위로: 섹션 상단(정렬 위치)보다 아래에 있으면 한 화면 올림
            if (scrollY > topAligned + EDGE) {
                scrollToY(Math.max(scrollY - step, topAligned));
            } else {
                goTo(current - 1);
            }
        }
    }

    /* 현재 뷰포트에 가장 가까운 섹션 인덱스 계산 */
    function getNearestSection() {
        const viewTop = window.scrollY + NAV_HEIGHT;
        let nearest = 0;
        let minDist = Infinity;
        sections.forEach((sec, i) => {
            const dist = Math.abs(viewTop - sec.offsetTop);
            if (dist < minDist) { minDist = dist; nearest = i; }
        });
        return nearest;
    }

    /* ── 개입하지 말아야 할 영역 판별 ── */

    // 지도 컨테이너 — index.html(#mainLocationMap)과 location.html(#map)
    function isOverMap(e) {
        const mapIds = ['mainLocationMap', 'map'];
        return mapIds.some(function (id) {
            const el = document.getElementById(id);
            return el && el.contains(e.target);
        });
    }

    function isPopupOpen() {
        const popup = document.getElementById('popupContainer');
        return popup && popup.classList.contains('show');
    }

    function isMenuOpen() {
        const menu = document.getElementById('mainMobileMenu');
        return menu && menu.classList.contains('open');
    }

    /* 휠 이벤트 */
    let lastWheelTime = 0;
    const WHEEL_COOLDOWN = 900;

    window.addEventListener('wheel', function (e) {
        if (isOverMap(e)) return;
        if (isPopupOpen()) return;

        e.preventDefault();

        if (isScrolling) return;

        const now = Date.now();
        if (now - lastWheelTime < WHEEL_COOLDOWN) return;
        if (Math.abs(e.deltaY) < 20) return;

        lastWheelTime = now;
        advance(e.deltaY > 0 ? 1 : -1);
    }, { passive: false });

    /* 터치 스와이프 */
    let touchStartY = 0;

    window.addEventListener('touchstart', function (e) {
        touchStartY = e.touches[0].clientY;
    }, { passive: true });

    // 한 번의 스와이프 = 한 단계 이동을 보장하기 위해
    // 지도/팝업/메뉴가 아닌 영역에서는 기본 스크롤(관성)을 막는다.
    window.addEventListener('touchmove', function (e) {
        if (isOverMap(e) || isPopupOpen() || isMenuOpen()) return;
        e.preventDefault();
    }, { passive: false });

    window.addEventListener('touchend', function (e) {
        if (isOverMap(e) || isPopupOpen() || isMenuOpen()) return;

        const diff = touchStartY - e.changedTouches[0].clientY;
        if (Math.abs(diff) < 40) return;

        advance(diff > 0 ? 1 : -1);
    }, { passive: true });

    /* 키보드 */
    window.addEventListener('keydown', function (e) {
        if (isPopupOpen()) return;
        if (['ArrowDown', 'PageDown'].includes(e.key)) {
            e.preventDefault();
            advance(1);
        } else if (['ArrowUp', 'PageUp'].includes(e.key)) {
            e.preventDefault();
            advance(-1);
        }
    });

    /* 리사이즈 시 현재 섹션 위치 재조정 (폭이 바뀐 경우만 — 모바일 주소창 높이 변화 무시) */
    let lastWidth = window.innerWidth;
    window.addEventListener('resize', function () {
        if (window.innerWidth === lastWidth) return; // 높이만 변한 경우(모바일 주소창) 무시
        lastWidth = window.innerWidth;
        if (!isScrolling) {
            current = getNearestSection();
            window.scrollTo({ top: sectionTop(current), behavior: 'instant' });
        }
    });
})();
