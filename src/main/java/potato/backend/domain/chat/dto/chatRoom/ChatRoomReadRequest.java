package potato.backend.domain.chat.dto.chatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 채팅방 전체 메시지 읽음 요청 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomReadRequest {

    private Long roomId;   // 채팅방 ID
    private Long memberId; // 읽은 사용자 ID

    public static ChatRoomReadRequest of(Long roomId, Long memberId) {
        return ChatRoomReadRequest.builder()
                .roomId(roomId)
                .memberId(memberId)
                .build();
    }
}
