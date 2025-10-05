package potato.backend.domain.chat.dto;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import potato.backend.domain.chat.domain.ChatRoom;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomResponse {

    private final Long chatRoomId;
    private final Long sellerId;
    private final String sellerName;
    private final Long buyerId;
    private final String buyerName;
    private final Instant createdAt;
    private final Instant updatedAt;

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
