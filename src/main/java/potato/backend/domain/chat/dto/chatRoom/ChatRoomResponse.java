package potato.backend.domain.chat.dto.chatRoom;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import potato.backend.domain.chat.domain.ChatRoom;

// 채팅방 조회 응답 DTO
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomResponse {

    private Long chatRoomId;
    private Long sellerId;
    private String sellerName;
    private Long buyerId;
    private String buyerName;
    private Instant createdAt;
    private Instant updatedAt;

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .sellerId(chatRoom.getSeller().getId())
                .sellerName(chatRoom.getSeller().getName())
                .buyerId(chatRoom.getBuyer().getId())
                .buyerName(chatRoom.getBuyer().getName())
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .build();
    }
}
