package potato.backend.domain.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import potato.backend.domain.common.domain.BaseEntity;
import potato.backend.domain.user.domain.Member;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Column(nullable = false)
    private Member userId;

    @OneToOne(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private Category categoryId;

    @Version
    private Long version; // 동시 수정에 따른 버전 별 관리

    @Column(nullable = false)
    private String title;

    @Lob // content의 길이를 제한하지 않음
    @Column(nullable = false)ß
    private String content;

    @Column(nullable = false, precision = 18, scale = 0) // 금액 관련 부분은 DB에 명시해 DB가 원하는 자릿수를 강제
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L;


    public static Product create(
            Member userId,
            Category categoryId,
            String title,
            String content,
            BigDecimal price,
            Status status
    ) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(categoryId, "categoryId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(status, "status");

        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must be >= 0");
        }

        // scale 고정 + 반올림 모드 명시 (금융 기본: HALF_UP)
        // 일반적으로는 원화라 정수이지만, 환율 계산 등으로 소수점이 발생할 때. 사용한다는 것을 인지하기
        BigDecimal normalized = price.setScale(0, RoundingMode.HALF_UP);

        return Product.builder()
                .userId(userId)
                .categoryId(categoryId)
                .title(title)
                .content(content)
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

}