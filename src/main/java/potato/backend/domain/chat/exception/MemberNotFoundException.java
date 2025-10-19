package potato.backend.domain.chat.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String message) {
        super(message);
    }

    public MemberNotFoundException(Long id) {
        super("사용자를 찾을 수 없습니다: " + id);
    }
}
