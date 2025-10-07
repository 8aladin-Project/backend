package potato.backend.domain.product.dto;

import lombok.*;
import potato.backend.domain.category.Category;
import potato.backend.domain.product.domain.Image;

import java.util.List;

@Getter
@Setter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCreateRequest {
    private Long memberId;
    private String title;
    private List<String> category;
    private String content;
    private String mainImageUrl;
    private List<Image> images;
    private Long price;
    private String status;

    public static ProductCreateRequest of(
            Long memberId,
            List<String> category,
            String title,
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
                images.stream().map(Image::create).toList(),
                price,
                status
        );
    }
}
