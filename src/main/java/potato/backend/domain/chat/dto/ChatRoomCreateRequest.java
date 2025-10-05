package potato.backend.domain.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 채팅방 생성 요청 DTO
@Getter
@Setter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomCreateRequest {

    @NotNull
    @Positive
    private Long sellerId;

    @NotNull
    @Positive
    private Long buyerId;


    public static ChatRoomCreateRequest of(Long sellerId, Long buyerId) {
        return new ChatRoomCreateRequest(sellerId, buyerId);
    }
}
