package com.church.website.exception;

/**
 * DB에서 요청한 엔티티를 찾을 수 없을 때 던지는 예외.
 *
 * <p>Service 레이어에서 findById() 결과가 없을 때 이 예외를 던지면,
 * GlobalExceptionHandler가 잡아서 404 에러 페이지(error/404)를 렌더링한다.
 *
 * <p>RuntimeException을 상속하므로 throws 선언 없이 사용할 수 있다.
 * (Checked Exception이 아니므로 호출자가 try-catch를 강제받지 않음)
 *
 * <p>사용 예시:
 * <pre>
 *   return noticeRepository.findById(id)
 *       .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
 * </pre>
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * @param message 사용자에게 보여줄 오류 메시지 (예: "공지사항을 찾을 수 없습니다.")
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
