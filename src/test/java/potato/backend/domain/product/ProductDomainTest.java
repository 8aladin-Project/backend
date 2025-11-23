package potato.backend.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.domain.Status;
import potato.backend.domain.product.domain.Condition;
import potato.backend.domain.user.domain.Member;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Product 도메인 테스트")
class ProductDomainTest {

        @Test
        @DisplayName("상품 생성 성공 테스트")
        void createProduct() {
                // given
                Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
                Category category = Category.create("전자기기");
                List<String> imageUrls = List.of("image1.jpg", "image2.jpg");

                // when
                Product product = Product.create(
                                member,
                                List.of(category),
                                "아이폰 15",
                                "새 제품입니다",
                                imageUrls,
                                BigDecimal.valueOf(1500000),
                                Status.SELLING,
                                "main.jpg",
                                Condition.NEW);

                // then
                assertThat(product).isNotNull();
                assertThat(product.getTitle()).isEqualTo("아이폰 15");
                assertThat(product.getContent()).isEqualTo("새 제품입니다");
                assertThat(product.getPrice()).isEqualTo(BigDecimal.valueOf(1500000));
                assertThat(product.getStatus()).isEqualTo(Status.SELLING);
                assertThat(product.getMainImageUrl()).isEqualTo("main.jpg");
                assertThat(product.getImages()).hasSize(2);
                assertThat(product.getCategories()).hasSize(1);
                assertThat(product.getMember()).isEqualTo(member);
        }

        @Test
        @DisplayName("상품 생성 시 member가 null이면 예외 발생")
        void createProductWithNullMember() {
                // given
                Category category = Category.create("전자기기");

                // when & then
                assertThatThrownBy(() -> Product.create(
                                null,
                                List.of(category),
                                "아이폰 15",
                                "새 제품입니다",
                                List.of("image1.jpg"),
                                BigDecimal.valueOf(1500000),
                                Status.SELLING,
                                "main.jpg",
                                Condition.NEW)).isInstanceOf(NullPointerException.class)
                                .hasMessageContaining("member");
        }

        @Test
        @DisplayName("상품 생성 시 가격이 음수면 예외 발생")
        void createProductWithNegativePrice() {
                // given
                Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
                Category category = Category.create("전자기기");

                // when & then
                assertThatThrownBy(() -> Product.create(
                                member,
                                List.of(category),
                                "아이폰 15",
                                "새 제품입니다",
                                List.of("image1.jpg"),
                                BigDecimal.valueOf(-1000),
                                Status.SELLING,
                                "main.jpg",
                                Condition.NEW)).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("price must be >= 0");
        }

        @Test
        @DisplayName("상품 생성 시 이미지가 비어있으면 예외 발생")
        void createProductWithEmptyImages() {
                // given
                Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
                Category category = Category.create("전자기기");

                // when & then
                assertThatThrownBy(() -> Product.create(
                                member,
                                List.of(category),
                                "아이폰 15",
                                "새 제품입니다",
                                List.of(),
                                BigDecimal.valueOf(1500000),
                                Status.SELLING,
                                "main.jpg",
                                Condition.NEW)).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("imageUrls must not be empty");
        }

        @Test
        @DisplayName("상품 정보 업데이트 테스트")
        void updateProduct() {
                // given
                Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
                Category category = Category.create("전자기기");

                Product product = Product.create(
                                member,
                                List.of(category),
                                "원래 제목",
                                "원래 내용",
                                List.of("image1.jpg"),
                                BigDecimal.valueOf(100000),
                                Status.SELLING,
                                "original.jpg",
                                Condition.NEW);

                // when
                product.update("수정된 제목", "수정된 내용", BigDecimal.valueOf(150000), Status.SOLD_OUT, "updated.jpg", null,
                                null);

                // then
                assertThat(product.getTitle()).isEqualTo("수정된 제목");
                assertThat(product.getContent()).isEqualTo("수정된 내용");
                assertThat(product.getPrice()).isEqualTo(BigDecimal.valueOf(150000));
                assertThat(product.getStatus()).isEqualTo(Status.SOLD_OUT);
                assertThat(product.getMainImageUrl()).isEqualTo("updated.jpg");
        }

        @Test
        @DisplayName("상품 부분 업데이트 테스트 - null 값은 업데이트하지 않음")
        void updateProductPartially() {
                // given
                Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
                Category category = Category.create("전자기기");

                Product product = Product.create(
                                member,
                                List.of(category),
                                "원래 제목",
                                "원래 내용",
                                List.of("image1.jpg"),
                                BigDecimal.valueOf(100000),
                                Status.SELLING,
                                "original.jpg",
                                Condition.NEW);

                // when - 제목만 업데이트
                product.update("수정된 제목만", null, null, null, null, null, null);

                // then
                assertThat(product.getTitle()).isEqualTo("수정된 제목만");
                assertThat(product.getContent()).isEqualTo("원래 내용");
                assertThat(product.getPrice()).isEqualTo(BigDecimal.valueOf(100000));
                assertThat(product.getStatus()).isEqualTo(Status.SELLING);
                assertThat(product.getMainImageUrl()).isEqualTo("original.jpg");
        }

        @Test
        @DisplayName("상품 업데이트 시 음수 가격이면 예외 발생")
        void updateProductWithNegativePrice() {
                // given
                Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
                Category category = Category.create("전자기기");

                Product product = Product.create(
                                member,
                                List.of(category),
                                "원래 제목",
                                "원래 내용",
                                List.of("image1.jpg"),
                                BigDecimal.valueOf(100000),
                                Status.SELLING,
                                "original.jpg",
                                Condition.NEW);

                // when & then
                assertThatThrownBy(() -> product.update(null, null, BigDecimal.valueOf(-5000), null, null, null, null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("price must be >= 0");
        }

        @Test
        @DisplayName("가격 반올림 테스트")
        void priceRounding() {
                // given
                Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
                Category category = Category.create("전자기기");

                // when
                Product product = Product.create(
                                member,
                                List.of(category),
                                "테스트 상품",
                                "가격 반올림 테스트",
                                List.of("image1.jpg"),
                                new BigDecimal("1234.56"),
                                Status.SELLING,
                                "main.jpg",
                                Condition.NEW);

                // then
                assertThat(product.getPrice()).isEqualTo(new BigDecimal("1235"));
        }

        @Test
        @DisplayName("상품 초기 조회수와 좋아요 수는 0이어야 함")
        void initialCountsShouldBeZero() {
                // given
                Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
                Category category = Category.create("전자기기");

                // when
                Product product = Product.create(
                                member,
                                List.of(category),
                                "테스트 상품",
                                "초기 카운트 테스트",
                                List.of("image1.jpg"),
                                BigDecimal.valueOf(50000),
                                Status.SELLING,
                                "main.jpg",
                                Condition.NEW);

                // then
                assertThat(product.getViewCount()).isEqualTo(0L);
                assertThat(product.getLikeCount()).isEqualTo(0L);
        }
}
