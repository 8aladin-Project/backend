package potato.backend.domain.wishlist.domain;

import jakarta.persistence.*;
import lombok.*;
import potato.backend.domain.common.domain.BaseEntity;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.user.domain.Member;

import java.util.Objects;

@Entity
@Table(name = "wishlist", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"member_id", "product_id"}) // 한 사용자가 한 상품을 한 번만 위시리스트에 추가할 수 있도록 제한
})
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 위시리스트 생성 팩토리 메서드
    public static Wishlist create(Member member, Product product) {
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(product, "product");

        return Wishlist.builder()
                .member(member)
                .product(product)
                .build();
    }
}