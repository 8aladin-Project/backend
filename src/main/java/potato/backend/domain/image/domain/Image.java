package potato.backend.domain.image.domain;

import jakarta.persistence.*;
import lombok.*;
import potato.backend.domain.common.domain.BaseEntity;
import potato.backend.domain.product.domain.Product;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        name = "image_seq",
        sequenceName = "image_seq",
        allocationSize = 50 // 캐싱 사이즈
)
public class Image extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_seq")
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product products;

    @Column(nullable = false)
    private String imageUrl;

    public static Image create(Product product, String imageUrl) {
        return Image.builder()
            .products(product)
            .imageUrl(imageUrl)
            .build();
    }
}
