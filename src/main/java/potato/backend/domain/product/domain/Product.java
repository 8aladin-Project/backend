package potato.backend.domain.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.common.domain.BaseEntity;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.user.domain.Member;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
        // 몇 개의 순차적 식별자를 미리 할당할지 설정, 일종의 캐쉬 개념
        name = "product_seq",
        sequenceName = "product_seq",
        allocationSize = 50 // 캐싱 사이즈
)
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // DB의 product 테이블에 생성될 외래 키 컬럼 이름을 'member_id'로 지정
    private Member member;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @Column(nullable = false)
    private String title;

    // @Lob // content의 길이를 제한하지 않음
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String mainImageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Image> images = new java.util.ArrayList<>();

    @Column(nullable = false, precision = 18, scale = 0) // 금액 관련 부분을 DB에 명시해 DB가 원하는 자릿수를 강제
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Long likeCount = 0L;


// == 생성 메서드 ==
    public static Product create(
            Member member,
            List<Category> categories,
            String title,
            String content,
            List<String> imageUrls,
            BigDecimal price,
            Status status,
            String mainImageUrl
    ) {
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(categories, "categories");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(mainImageUrl, "mainImageUrl");
        Objects.requireNonNull(imageUrls, "imageUrls");

        if (categories.isEmpty()) {
            throw new IllegalArgumentException("categories must not be empty");
        }

        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must be >= 0");
        }

        if (imageUrls.isEmpty()) {
            throw new IllegalArgumentException("imageUrls must not be empty");
        }

        // scale 고정 + 반올림 모드 명시 (금융 기본: HALF_UP)
        BigDecimal normalized = price.setScale(0, RoundingMode.HALF_UP);

        Product product = Product.builder()
                .member(member)
                .categories(new java.util.ArrayList<>(categories))
                .title(title)
                .content(content)
                .mainImageUrl(mainImageUrl)
                .price(normalized)
                .status(status)
                .build();

        // 이미지 URL 문자열로부터 Image 엔티티 생성 및 추가
        List<Image> images = imageUrls.stream()
                .map(imageUrl -> {
                    Image image = Image.create(imageUrl);
                    image.setProduct(product);
                    return image;
                })
                .toList();
        product.images.addAll(images);

        return product;
    }

    // === proxy-safe equals/hashCode (ID가 있을 때만 동등) ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        // Hibernate 프록시까지 고려한 타입 비교
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Product other = (Product) o;
        // 영속 전에는 ID가 없으므로 '동일 인스턴스' 외에는 같지 않다
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // 영속 전 해시 불안정성 방지: 클래스 기반 상수 해시
        return Hibernate.getClass(this).hashCode();
    }

    // == 비즈니스 메서드 ==
    public void update(String title, String content, BigDecimal price, Status status, String mainImageUrl) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (price != null) {
            if (price.signum() < 0) {
                throw new IllegalArgumentException("price must be >= 0");
            }
            this.price = price.setScale(0, RoundingMode.HALF_UP);
        }
        if (status != null) {
            this.status = status;
        }
        if (mainImageUrl != null) {
            this.mainImageUrl = mainImageUrl;
        }
    }

}
