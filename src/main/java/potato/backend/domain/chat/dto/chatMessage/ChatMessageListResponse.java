package potato.backend.domain.chat.dto.chatMessage;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅 메시지 목록 조회 응답 DTO
 */
@Getter
@Schema(description = "채팅 메시지 목록 조회 응답")
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageListResponse {

    private boolean success;
    private Data data;

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Schema(description = "응답 데이터")
    public static class Data {
        private List<Message> messages;
        private boolean hasMore;
        private String nextCursor;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Schema(description = "메시지 정보")
    public static class Message {
        private String id;
        private String senderId;
        private String senderName;
        private String content;
        private String messageType;
        private String timestamp;
        private boolean isRead;
        private String readAt;
        private Map<String, Object> metadata;
    }

    public static ChatMessageListResponse success(List<Message> messages, boolean hasMore, String nextCursor) {
        Data data = Data.builder()
                .messages(messages)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();

        return ChatMessageListResponse.builder()
                .success(true)
                .data(data)
                .build();
    }

    public static Message ofMessage(potato.backend.domain.chat.domain.ChatMessage chatMessage) {
        return Message.builder()
                .id(chatMessage.getId().toString())
                .senderId(chatMessage.getSender().getId().toString())
                .senderName(chatMessage.getSender().getName())
                .content(chatMessage.getContent())
                .messageType("text") // 기본적으로 text로 설정
                .timestamp(chatMessage.getSentAt().toString())
                .isRead(chatMessage.isRead())
                .readAt(null) // 현재 엔티티에 readAt 필드가 없으므로 null로 설정
                .metadata(Map.of()) // 빈 맵으로 초기화
                .build();
    }
}
