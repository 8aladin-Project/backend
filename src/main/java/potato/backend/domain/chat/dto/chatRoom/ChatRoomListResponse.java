package potato.backend.domain.chat.dto.chatRoom;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 목록 조회 응답 DTO
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomListResponse {

    private boolean success;
    private Data data;

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Data {
        private List<ChatRoomSummary> rooms;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChatRoomSummary {
        private String id;
        private String userName;      // 상대방 이름
        private String userId;        // 상대방 ID
        private String productImage;  // 상품 이미지 URL
        private String productName;   // 상품명
        private String productId;     // 상품 ID
        private long price;           // 상품 가격
        private String lastMessage;   // 마지막 메시지 내용
        private String timestamp;     // 마지막 메시지 시간
        private long unreadCount;     // 읽지 않은 메시지 수
        private boolean isOnline;     // 상대방 온라인 상태
    }

    public static ChatRoomListResponse success(List<ChatRoomSummary> rooms) {
        Data data = Data.builder()
                .rooms(rooms)
                .build();

        return ChatRoomListResponse.builder()
                .success(true)
                .data(data)
                .build();
    }

    public static ChatRoomSummary ofRoom(
            Long roomId,
            String userName,
            String userId,
            String productImage,
            String productName,
            String productId,
            Long price,
            String lastMessage,
            String timestamp,
            Long unreadCount,
            Boolean isOnline) {

        return ChatRoomSummary.builder()
                .id(roomId.toString())
                .userName(userName)
                .userId(userId)
                .productImage(productImage)
                .productName(productName)
                .productId(productId)
                .price(price != null ? price : 0)
                .lastMessage(lastMessage)
                .timestamp(timestamp)
                .unreadCount(unreadCount != null ? unreadCount : 0)
                .isOnline(isOnline != null ? isOnline : false)
                .build();
    }
}
