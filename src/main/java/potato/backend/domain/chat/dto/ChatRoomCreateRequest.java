package potato.backend.domain.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomCreateRequest {

    @NotNull
    @Positive
    private Long sellerId;

    @NotNull
    @Positive
    private Long buyerId;

    public ChatRoomCreateRequest(Long sellerId, Long buyerId) {
        this.sellerId = sellerId;
        this.buyerId = buyerId;
    }
}
