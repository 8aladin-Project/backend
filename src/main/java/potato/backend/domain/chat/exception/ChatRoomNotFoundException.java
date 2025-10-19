package potato.backend.domain.chat.exception;

public class ChatRoomNotFoundException extends RuntimeException {
    public ChatRoomNotFoundException(String message) {
        super(message);
    }

    public ChatRoomNotFoundException(Long id) {
        super("채팅방을 찾을 수 없습니다: " + id);
    }
}
