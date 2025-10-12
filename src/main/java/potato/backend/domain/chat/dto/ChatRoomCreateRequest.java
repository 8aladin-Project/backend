package potato.backend.domain.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

// 채팅방 생성 요청 DTO
@Getter
@Setter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomCreateRequest {

    @Schema(name = "seller_id", description = "판매자 회원 식별자", example = "1")
    @NotNull
    @Positive
    private Long sellerId;

    @Schema(name = "buyer_id", description = "구매자 회원 식별자", example = "2")
    @NotNull
    @Positive
    private Long buyerId;


    public static ChatRoomCreateRequest of(Long sellerId, Long buyerId) {
        return new ChatRoomCreateRequest(sellerId, buyerId);
    }
}
