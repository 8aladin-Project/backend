package potato.backend.domain.product.dto;

import lombok.Getter;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.product.domain.Product;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProductListResponse {
    private Long id;
    private Long productId;
    private List<String> category;
    private String title;
    private Long price;
    private String mainImageUrl;
    private String status;
    private Long likeCount;
    private Long viewCount;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductListResponse fromEntity(Product product) {
        ProductListResponse response = new ProductListResponse();
        response.id = product.getMember().getId();
        response.productId = product.getId();
        response.title = product.getTitle();
        response.category = product.getCategories()
                .stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
        response.price = product.getPrice().longValue();
        response.mainImageUrl = product.getMainImageUrl();
        response.status = product.getStatus().name();
        response.likeCount = product.getLikeCount();
        response.viewCount = product.getViewCount();
        response.createdAt = product.getCreatedAt();
        response.updatedAt = product.getUpdatedAt();
        return response;
    }
}
