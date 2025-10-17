package potato.backend.domain.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WishlistResponse {
    private String message;
    private Long memberId;
    private Long productId;
    
    public static WishlistResponse of(String message, Long memberId, Long productId) {
        return new WishlistResponse(message, memberId, productId);
    }
}
