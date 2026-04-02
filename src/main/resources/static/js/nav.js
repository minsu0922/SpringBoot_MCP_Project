/**
 * 공통 모바일 햄버거 메뉴
 */
(function () {
    const hamburger = document.getElementById('hamburger');
    const mobileMenu = document.getElementById('mobileMenu');

    if (!hamburger || !mobileMenu) return;

    hamburger.addEventListener('click', function () {
        const isOpen = mobileMenu.classList.toggle('open');
        hamburger.classList.toggle('open', isOpen);
        hamburger.setAttribute('aria-label', isOpen ? '메뉴 닫기' : '메뉴 열기');
        document.body.style.overflow = isOpen ? 'hidden' : '';
    });

    /* 메뉴 링크 클릭 시 닫기 */
    mobileMenu.querySelectorAll('a').forEach(function (link) {
        link.addEventListener('click', function () {
            mobileMenu.classList.remove('open');
            hamburger.classList.remove('open');
            document.body.style.overflow = '';
        });
    });

    /* 바깥 클릭 시 닫기 */
    document.addEventListener('click', function (e) {
        if (!hamburger.contains(e.target) && !mobileMenu.contains(e.target)) {
            mobileMenu.classList.remove('open');
            hamburger.classList.remove('open');
            document.body.style.overflow = '';
        }
    });
})();
