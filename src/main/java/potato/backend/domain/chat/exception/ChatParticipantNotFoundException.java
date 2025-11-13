package potato.backend.domain.chat.exception;

public class ChatParticipantNotFoundException extends RuntimeException {
    public ChatParticipantNotFoundException(String message) {
        super(message);
    }

    public ChatParticipantNotFoundException(Long memberId, Long roomId) {
        super("사용자 " + memberId + "는 채팅방 " + roomId + "에 참여하지 않았습니다");
    }
}
