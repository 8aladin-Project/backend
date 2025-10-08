package potato.backend.domain.product.dto;

import lombok.Getter;
import potato.backend.domain.category.Category;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.product.domain.Product;

import java.util.List;

@Getter
public class ProductResponse {
    private Long id;
    private List<Category> categories;
    private String title;
    private String content;
    private Long price;
    private List<Image> images;
    private String status;
    private Long likeCount;
    private Long viewCount;
    private String createdAt;
    private String updatedAt;

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.id = product.getId();
        response.categories = product.getCategories();
        response.title = product.getTitle();
        response.content = product.getContent();
        response.price = product.getPrice().longValue();
        response.images = product.getImages();
        response.status = product.getStatus().name();
        response.likeCount = product.getLikeCount();
        response.viewCount = product.getViewCount();
        response.createdAt = product.getCreatedAt().toString();
        response.updatedAt = product.getUpdatedAt().toString();
        return response;
    }
}