package potato.backend.domain.chat.dto.chatMessage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 채팅 메시지 전송 요청 DTO
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSendRequest {

    @NotNull
    private Long senderId;

    @NotBlank
    private String content;

    public static ChatSendRequest of(Long senderId, String content) {
        return new ChatSendRequest(senderId, content);
    }
}
