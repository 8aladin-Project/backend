package potato.backend.domain.chat.dto.chatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 읽지 않은 메시지 개수 조회 응답 DTO
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatUnreadCountResponse {

    private Long memberId;      // 사용자 ID
    private Long roomId;        // 채팅방 ID (전체 조회 시 null)
    private long unreadCount;   // 읽지 않은 메시지 개수

    // 유저가 읽지 않은 메시지의 총 개수를 조회하는 메서드
    public static ChatUnreadCountResponse ofTotal(Long memberId, long unreadCount) {
        return ChatUnreadCountResponse.builder()
                .memberId(memberId)
                .unreadCount(unreadCount)
                .build();
    }

    // 채팅방 하나에서 읽지 않은 메시지의 개수를 조회하는 메서드
    public static ChatUnreadCountResponse ofRoom(Long memberId, Long roomId, long unreadCount) {
        return ChatUnreadCountResponse.builder()
                .memberId(memberId)
                .roomId(roomId)
                .unreadCount(unreadCount)
                .build();
    }
}
