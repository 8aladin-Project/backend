package potato.backend.domain.chat.dto.chatMessage;

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
    @Positive // 해당 값은 양수만 허용
    private Long sellerId;

    @Schema(name = "buyer_id", description = "구매자 회원 식별자", example = "2")
    @NotNull
    @Positive
    private Long buyerId;

    @Schema(name = "product_id", description = "상품 식별자", example = "1")
    @NotNull
    @Positive
    private Long productId;


    public static ChatRoomCreateRequest of(Long sellerId, Long buyerId, Long productId) {
        return ChatRoomCreateRequest.builder()
                .sellerId(sellerId)
                .buyerId(buyerId)
                .productId(productId)
                .build();
    }

    public static ChatRoomCreateRequest of(Long sellerId, Long buyerId) {
        return ChatRoomCreateRequest.builder()
                .sellerId(sellerId)
                .buyerId(buyerId)
                .build();
    }
}
