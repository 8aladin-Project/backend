package potato.backend.domain.chat.exception;

public class InvalidChatRoomParticipantsException extends RuntimeException {
    public InvalidChatRoomParticipantsException(String message) {
        super(message);
    }

    public InvalidChatRoomParticipantsException() {
        super("판매자와 구매자는 다른 사용자여야 합니다");
    }
}
