package potato.backend.domain.product.dto;

import lombok.Getter;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.image.dto.ImageResponse;
import potato.backend.domain.product.domain.Product;

import java.util.List;

@Getter
public class ProductResponse {
    private Long id;
    private Long productId;
    private List<Category> categories;
    private String title;
    private String content;
    private Long price;
    private List<ImageResponse> images;
    private String status;
    private Long likeCount;
    private Long viewCount;
    private String createdAt;
    private String updatedAt;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.id = product.getMember().getId();
        response.productId = product.getId();
        response.categories = product.getCategories();
        response.title = product.getTitle();
        response.content = product.getContent();
        response.price = product.getPrice().longValue();
        response.images = ImageResponse.fromList(product.getImages());
        response.status = product.getStatus().name();
        response.likeCount = product.getLikeCount();
        response.viewCount = product.getViewCount();
        response.createdAt = product.getCreatedAt() != null ? product.getCreatedAt().toString() : null;
        response.updatedAt = product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : null;
        return response;
    }
}