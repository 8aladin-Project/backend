package potato.backend.domain.wishlist.dto;

import lombok.Getter;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.wishlist.domain.Wishlist;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class WishlistListResponse {
    private Long productId;
    private Long sellerId;
    private List<String> category;
    private String title;
    private Long price;
    private String mainImageUrl;
    private String status;
    private Long likeCount;
    private Long viewCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant wishlistCreatedAt; // 위시리스트에 추가된 시간

    public static WishlistListResponse fromEntity(Wishlist wishlist) {
        WishlistListResponse response = new WishlistListResponse();
        Product product = wishlist.getProduct();
        
        response.productId = product.getId();
        response.sellerId = product.getMember().getId();
        response.category = product.getCategories()
                .stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
        response.title = product.getTitle();
        response.price = product.getPrice().longValue();
        response.mainImageUrl = product.getMainImageUrl();
        response.status = product.getStatus().name();
        response.likeCount = product.getLikeCount();
        response.viewCount = product.getViewCount();
        response.createdAt = product.getCreatedAt();
        response.updatedAt = product.getUpdatedAt();
        response.wishlistCreatedAt = wishlist.getCreatedAt();
        
        return response;
    }
}
