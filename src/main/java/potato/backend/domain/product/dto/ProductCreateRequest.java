package potato.backend.domain.product.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductCreateRequest {
    private final Long memberId;
    private final String title;
    private final List<String> category;
    private final String content;
    private final String mainImageUrl;
    private final List<String> images;
    private final Long price;
    private final String status;

    public static ProductCreateRequest of (
            Long memberId,
            String title,
            List<String> category,
            String content,
            String mainImageUrl,
            List<String> images,
            Long price,
            String status
    ) {
        return new ProductCreateRequest(
                memberId,
                title,
                category,
                content,
                mainImageUrl,
                images,
                price,
                status
        );
    }
}
