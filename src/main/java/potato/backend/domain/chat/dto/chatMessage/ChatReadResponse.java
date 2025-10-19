package potato.backend.domain.chat.dto.chatMessage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatReadResponse {

    private Long messageId;     // 읽음 처리된 메시지 ID (단일 메시지 읽음 처리 시)
    private Long roomId;        // 채팅방 ID (채팅방 전체 읽음 처리 시)
    private Long memberId;      // 읽은 사용자 ID
    private int readCount;      // 읽음 처리된 메시지 개수
    private boolean success;    // 읽음 처리 성공 여부

    public static ChatReadResponse ofMessage(Long messageId, Long memberId, boolean success) {
        return ChatReadResponse.builder()
                .messageId(messageId)
                .memberId(memberId)
                .readCount(success ? 1 : 0)
                .success(success)
                .build();
    }

    public static ChatReadResponse ofRoom(Long roomId, Long memberId, int readCount, boolean success) {
        return ChatReadResponse.builder()
                .roomId(roomId)
                .memberId(memberId)
                .readCount(readCount)
                .success(success)
                .build();
    }
}
