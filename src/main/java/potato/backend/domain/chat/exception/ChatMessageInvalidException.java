package potato.backend.domain.chat.exception;

public class ChatMessageInvalidException extends RuntimeException {
    public ChatMessageInvalidException(String message) {
        super(message);
    }

    public ChatMessageInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
