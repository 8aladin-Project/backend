package potato.backend.domain.chat.exception;

public class ChatMessageNotFoundException extends RuntimeException {
    public ChatMessageNotFoundException(String message) {
        super(message);
    }

    public ChatMessageNotFoundException(Long id) {
        super("메시지를 찾을 수 없습니다: " + id);
    }
}
