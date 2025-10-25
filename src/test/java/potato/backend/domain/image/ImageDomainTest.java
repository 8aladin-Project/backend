package potato.backend.domain.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.domain.Status;
import potato.backend.domain.user.domain.Member;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Image 도메인 테스트")
class ImageDomainTest {

    @Test
    @DisplayName("이미지 생성 테스트")
    void createImage() {
        // given
        Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
        Category category = Category.create("전자기기");

        Product product = Product.create(
                member,
                List.of(category),
                "테스트 상품",
                "테스트 설명",
                List.of("image1.jpg"),
                BigDecimal.valueOf(100000),
                Status.SELLING,
                "main.jpg"
        );

        // when
        Image image = Image.create(product, "test-image.jpg");

        // then
        assertThat(image).isNotNull();
        assertThat(image.getImageUrl()).isEqualTo("test-image.jpg");
        assertThat(image.getProduct()).isEqualTo(product);
    }

    @Test
    @DisplayName("이미지 생성 시 Product가 null이면 예외 발생")
    void createImageWithNullProduct() {
        // when & then
        assertThatThrownBy(() -> Image.create(null, "test-image.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product cannot be null");
    }

    @Test
    @DisplayName("이미지 생성 시 imageUrl이 null이면 예외 발생")
    void createImageWithNullUrl() {
        // given
        Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
        Category category = Category.create("전자기기");

        Product product = Product.create(
                member,
                List.of(category),
                "테스트 상품",
                "테스트 설명",
                List.of("image1.jpg"),
                BigDecimal.valueOf(100000),
                Status.SELLING,
                "main.jpg"
        );

        // when & then
        assertThatThrownBy(() -> Image.create(product, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image URL cannot be null or empty");
    }

    @Test
    @DisplayName("이미지 생성 시 imageUrl이 빈 문자열이면 예외 발생")
    void createImageWithEmptyUrl() {
        // given
        Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
        Category category = Category.create("전자기기");

        Product product = Product.create(
                member,
                List.of(category),
                "테스트 상품",
                "테스트 설명",
                List.of("image1.jpg"),
                BigDecimal.valueOf(100000),
                Status.SELLING,
                "main.jpg"
        );

        // when & then
        assertThatThrownBy(() -> Image.create(product, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image URL cannot be null or empty");
    }

    @Test
    @DisplayName("이미지 URL이 올바르게 저장되는지 테스트")
    void imageUrlStoredCorrectly() {
        // given
        Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
        Category category = Category.create("전자기기");

        Product product = Product.create(
                member,
                List.of(category),
                "테스트 상품",
                "테스트 설명",
                List.of("image1.jpg"),
                BigDecimal.valueOf(100000),
                Status.SELLING,
                "main.jpg"
        );

        // when
        String imageUrl = "https://example.com/images/product1.jpg";
        Image image = Image.create(product, imageUrl);

        // then
        assertThat(image.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("이미지가 Product와 연관되어 있는지 테스트")
    void imageAssociatedWithProduct() {
        // given
        Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
        Category category = Category.create("전자기기");

        Product product = Product.create(
                member,
                List.of(category),
                "테스트 상품",
                "테스트 설명",
                List.of("image1.jpg"),
                BigDecimal.valueOf(100000),
                Status.SELLING,
                "main.jpg"
        );

        // when
        Image image = Image.create(product, "associated-image.jpg");

        // then
        assertThat(image.getProduct()).isNotNull();
        assertThat(image.getProduct().getTitle()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("Product 생성 시 여러 이미지가 자동으로 생성되는지 테스트")
    void multipleImagesCreatedWithProduct() {
        // given
        Member member = Member.create("테스트유저", "test@example.com", "password", "010-1234-5678");
        Category category = Category.create("전자기기");
        List<String> imageUrls = List.of("image1.jpg", "image2.jpg", "image3.jpg");

        // when
        Product product = Product.create(
                member,
                List.of(category),
                "테스트 상품",
                "여러 이미지 테스트",
                imageUrls,
                BigDecimal.valueOf(100000),
                Status.SELLING,
                "main.jpg"
        );

        // then
        assertThat(product.getImages()).hasSize(3);
        assertThat(product.getImages()).allMatch(img -> img.getProduct() == product);
        assertThat(product.getImages()).extracting(Image::getImageUrl)
                .containsExactlyInAnyOrder("image1.jpg", "image2.jpg", "image3.jpg");
    }
}

