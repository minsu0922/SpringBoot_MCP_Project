// 이미지 슬라이더 자동 재생 스크립트
document.addEventListener('DOMContentLoaded', function() {
    const slider = document.querySelector('.slider-wrapper');
    const dots = document.querySelectorAll('.slider-dot');
    let currentSlide = 0;
    const totalSlides = 3;
    const slideInterval = 4000; // 4초마다 슬라이드 변경

    // 슬라이드 이동 함수
    function goToSlide(slideIndex) {
        currentSlide = slideIndex;
        slider.style.transform = `translateX(-${currentSlide * 100}%)`;
        
        // 활성 도트 업데이트
        dots.forEach((dot, index) => {
            if (index === currentSlide) {
                dot.classList.add('active');
            } else {
                dot.classList.remove('active');
            }
        });
    }

    // 다음 슬라이드로 이동
    function nextSlide() {
        currentSlide = (currentSlide + 1) % totalSlides;
        goToSlide(currentSlide);
    }

    // 자동 재생
    let autoPlay = setInterval(nextSlide, slideInterval);

    // 도트 클릭 이벤트
    dots.forEach((dot, index) => {
        dot.addEventListener('click', () => {
            clearInterval(autoPlay);
            goToSlide(index);
            autoPlay = setInterval(nextSlide, slideInterval);
        });
    });

    // 마우스 호버 시 자동 재생 일시 정지
    const sliderContainer = document.querySelector('.slider');
    if (sliderContainer) {
        sliderContainer.addEventListener('mouseenter', () => {
            clearInterval(autoPlay);
        });

        sliderContainer.addEventListener('mouseleave', () => {
            autoPlay = setInterval(nextSlide, slideInterval);
        });
    }
});
