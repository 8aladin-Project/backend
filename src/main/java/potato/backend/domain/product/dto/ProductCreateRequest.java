package potato.backend.domain.product.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductCreateRequest {

    @Schema(description = "회원 ID", example = "1")
    private final Long memberId;

    @Schema(description = "상품 제목", example = "어쩌구")
    private final String title;

    @Schema(description = "카테고리 목록", example = "[\"디지털\", \"가전\"]")
    private final List<String> category;

    @Schema(description = "상품 설명", example = "상세 설명")
    private final String content;

    @Schema(description = "대표 이미지 URL", example = "https://api-dev-minio.8aladin.shop/paladin/images/main.png")
    private final String mainImageUrl;

    @Schema(description = "추가 이미지 URL 목록", example = "[\"https://.../image1.png\", \"https://.../image2.png\"]")
    private final List<String> images;

    @Schema(description = "가격(원)", example = "3000")
    private final Long price;

    @Schema(description = "판매 상태", example = "SELLING")
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
