package potato.backend.domain.chat.dto.chatMessage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 채팅 메시지 읽음 요청 DTO
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatMessageReadRequest {

    private Long messageId; // 읽음 처리할 메시지 ID
    private Long memberId;  // 읽은 사용자 ID

    public static ChatMessageReadRequest of(Long messageId, Long memberId) {
        return ChatMessageReadRequest.builder()
                .messageId(messageId)
                .memberId(memberId)
                .build();
    }
}
