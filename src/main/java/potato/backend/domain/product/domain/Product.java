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
    @JoinColumn(nullable = false)
    private Member member;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @Column(nullable = false)
    private String title;

    @Lob // content의 길이를 제한하지 않음
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String mainImageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
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
            Member memberId,
            List<Category> categoryId,
            String title,
            String content,
            List<Image> images,
            BigDecimal price,
            Status status,
            String mainImageUrl

    ) {
        Objects.requireNonNull(memberId, "memberId");
        Objects.requireNonNull(categoryId, "categoryId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(mainImageUrl, "mainImageUrl");
        Objects.requireNonNull(images, "images");

        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must be >= 0");
        }

        if (images.isEmpty()) {
            throw new IllegalArgumentException("images must not be empty");
        }

        // scale 고정 + 반올림 모드 명시 (금융 기본: HALF_UP)
        // 일반적으로는 원화라 정수이지만, 환율 계산 등으로 소수점이 발생할 때. 사용한다는 것을 인지하기
        BigDecimal normalized = price.setScale(0, RoundingMode.HALF_UP);

        return Product.builder()
                .member(memberId)
                .categories(categoryId)
                .title(title)
                .content(content)
                .mainImageUrl(mainImageUrl)
                .images(images)
                .price(normalized)
                .status(status)
                .build();
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
