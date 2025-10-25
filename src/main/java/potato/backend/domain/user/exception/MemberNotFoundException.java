package potato.backend.domain.user.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long memberId) {
        super("member not found with id: " + memberId);
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}
