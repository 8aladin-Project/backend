package potato.backend.domain.user.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long memberId) {
        super("memver not found with id: " + memberId);
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}
