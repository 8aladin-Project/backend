package potato.backend.domain.product.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductUpdateRequest {
    private final Long productId;
    private final String title;
    private final String content;
    private final String mainImageUrl;
    private final List<String> imageUrls;
    private final Long price;
    private final String status;
}
