package potato.backend.domain.product.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductUpdateRequest {
    private String title;
    private String content;
    private String mainImageUrl;
    private List<String> imageUrls;
    private String status;

    public static ProductUpdateRequest of (
            String title,
            String content,
            String mainImageUrl,
            List<String> imageUrls,
            String status
    ) {
        return new ProductUpdateRequest(
                title,
                content,
                mainImageUrl,
                imageUrls,
                status
        );
    }
}
