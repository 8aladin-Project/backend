package potato.backend.domain.product.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductUpdateRequest {

    @Schema(description = "상품 제목", example = "어쩌구-수정")
    private final String title;

    @Schema(description = "상품 설명", example = "수정된 상세 설명")
    private final String content;

    @Schema(description = "대표 이미지 URL", example = "https://api-dev-minio.8aladin.shop/paladin/images/main.png")
    private final String mainImageUrl;

    @Schema(description = "추가 이미지 URL 목록", example = "[\"https://.../image1.png\", \"https://.../image2.png\"]")
    private final List<String> imageUrls;

    @Schema(description = "가격(원)", example = "3500")
    private final Long price;

    @Schema(description = "판매 상태", example = "RESERVED")
    private final String status;

    @Schema(description = "상품 상태", example = "USED")
    private final String condition;
}
