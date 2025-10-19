package potato.backend.domain.chat.dto.chatMessage;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import potato.backend.domain.chat.domain.ChatMessage;

// 채팅 메시지 조회 응답 DTO
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageResponse {

    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String content;
    private boolean read;
    private Instant sentAt;

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.isRead(),
                message.getSentAt()
        );
    }
}
