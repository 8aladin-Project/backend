package potato.backend.domain.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.category.repository.CategoryRepository;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.image.repository.ImageRepository;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.domain.Status;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Image Repository 테스트")
class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        Member testMember = Member.create("테스트유저", "test@example.com", "hashedPassword", "010-1234-5678");
        memberRepository.save(testMember);

        // 테스트용 카테고리 생성
        Category testCategory = Category.create("전자기기");
        categoryRepository.save(testCategory);

        // 테스트용 상품 생성
        testProduct = Product.create(
                testMember,
                List.of(testCategory),
                "테스트 상품",
                "테스트 설명",
                List.of("image1.jpg", "image2.jpg"),
                BigDecimal.valueOf(100000),
                Status.SELLING,
                "main.jpg"
        );
        productRepository.save(testProduct);
    }

    @Test
    @DisplayName("이미지 생성 및 저장 테스트")
    void createAndSaveImage() {
        // given
        Image image = Image.create(testProduct, "test-image.jpg");

        // when
        Image savedImage = imageRepository.save(image);

        // then
        assertThat(savedImage).isNotNull();
        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getImageUrl()).isEqualTo("test-image.jpg");
        assertThat(savedImage.getProduct()).isEqualTo(testProduct);
    }

    @Test
    @DisplayName("상품으로 이미지 목록 조회 테스트")
    void findByProduct() {
        // given
        Image image1 = Image.create(testProduct, "product-image-1.jpg");
        Image image2 = Image.create(testProduct, "product-image-2.jpg");
        Image image3 = Image.create(testProduct, "product-image-3.jpg");
        imageRepository.saveAll(List.of(image1, image2, image3));

        // when
        List<Image> images = imageRepository.findByProduct(testProduct);

        // then
        assertThat(images).hasSize(5); // setUp에서 생성된 2개 + 새로 추가된 3개
        assertThat(images).extracting(Image::getImageUrl)
                .contains("product-image-1.jpg", "product-image-2.jpg", "product-image-3.jpg");
    }

    @Test
    @DisplayName("상품 ID로 이미지 목록 조회 테스트")
    void findByProductId() {
        // given
        Long productId = testProduct.getId();

        // when
        List<Image> images = imageRepository.findByProductId(productId);

        // then
        assertThat(images).hasSize(2); // setUp에서 생성된 2개
        assertThat(images).allMatch(image -> image.getProduct().getId().equals(productId));
    }

    @Test
    @DisplayName("이미지 삭제 테스트")
    void deleteImage() {
        // given
        Image image = Image.create(testProduct, "delete-image.jpg");
        Image savedImage = imageRepository.save(image);
        Long imageId = savedImage.getId();

        // when
        imageRepository.deleteById(imageId);

        // then
        assertThat(imageRepository.findById(imageId)).isEmpty();
    }

    @Test
    @DisplayName("여러 이미지 일괄 저장 테스트")
    void saveAllImages() {
        // given
        List<Image> images = List.of(
                Image.create(testProduct, "bulk-image-1.jpg"),
                Image.create(testProduct, "bulk-image-2.jpg"),
                Image.create(testProduct, "bulk-image-3.jpg")
        );

        // when
        List<Image> savedImages = imageRepository.saveAll(images);

        // then
        assertThat(savedImages).hasSize(3);
        assertThat(savedImages).allMatch(image -> image.getId() != null);
    }

    @Test
    @DisplayName("이미지 개수 조회 테스트")
    void countImages() {
        // given
        Image image1 = Image.create(testProduct, "count-image-1.jpg");
        Image image2 = Image.create(testProduct, "count-image-2.jpg");
        imageRepository.saveAll(List.of(image1, image2));

        // when
        long count = imageRepository.count();

        // then
        assertThat(count).isEqualTo(4); // setUp에서 생성된 2개 + 새로 추가된 2개
    }
}

