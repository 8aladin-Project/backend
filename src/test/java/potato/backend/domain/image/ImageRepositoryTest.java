package potato.backend.domain.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.category.repository.CategoryRepository;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.image.repository.ImageRepository;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.domain.Status;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;

import java.io.IOException;
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
    private String testImagePath1;
    private String testImagePath2;
    private String productImagePath1;
    private String productImagePath2;
    private String productImagePath3;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 이미지 파일 경로 설정
        testImagePath1 = new ClassPathResource("images/test-image-2.png").getFile().getAbsolutePath();
        testImagePath2 = new ClassPathResource("images/test-image-3.png").getFile().getAbsolutePath();
        productImagePath1 = new ClassPathResource("images/test-image-4.png").getFile().getAbsolutePath();
        productImagePath2 = new ClassPathResource("images/test-image-5.png").getFile().getAbsolutePath();
        productImagePath3 = new ClassPathResource("images/test-image-6.png").getFile().getAbsolutePath();

        // 테스트용 회원 생성
        Member testMember = Member.create("테스트유저", "test@example.com", "hashedPassword", "010-1234-5678");
        memberRepository.save(testMember);

        // 테스트용 카테고리 생성
        Category testCategory = Category.create("전자기기");
        categoryRepository.save(testCategory);

        // 테스트용 상품 생성 (실제 이미지 경로 사용)
        testProduct = Product.create(
                testMember,
                List.of(testCategory),
                "테스트 상품",
                "테스트 설명",
                List.of(testImagePath1, testImagePath2),
                BigDecimal.valueOf(100000),
                Status.SELLING,
                testImagePath1
        );
        productRepository.save(testProduct);
    }

    @Test
    @DisplayName("이미지 생성 및 저장 테스트")
    void createAndSaveImage() throws IOException {
        // given
        String imageUrl = new ClassPathResource("images/test-image-3.png").getFile().getAbsolutePath();
        Image image = Image.create(imageUrl);

        // when
        Image savedImage = imageRepository.save(image);

        // then
        assertThat(savedImage).isNotNull();
        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getImageUrl()).isEqualTo(imageUrl);
        assertThat(savedImage.getImageUrl()).contains("test-image-3.png");
    }

    @Test
    @DisplayName("상품으로 이미지 목록 조회 테스트")
    void findByProduct() {
        // given
        Image image1 = Image.create(productImagePath1);
        Image image2 = Image.create(productImagePath2);
        Image image3 = Image.create(productImagePath3);
        imageRepository.saveAll(List.of(image1, image2, image3));

        // when
        List<Image> images = imageRepository.findByProduct(testProduct);

        // then
        assertThat(images).hasSize(2); // setUp에서 생성된 2개
        assertThat(images).extracting(Image::getImageUrl)
                .contains(testImagePath1, testImagePath2);
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
        assertThat(images).extracting(Image::getImageUrl)
                .contains(testImagePath1, testImagePath2);
    }

    @Test
    @DisplayName("이미지 삭제 테스트")
    void deleteImage() throws IOException {
        // given
        String deleteImagePath = new ClassPathResource("images/test-image-4.png").getFile().getAbsolutePath();
        Image image = Image.create(deleteImagePath);
        Image savedImage = imageRepository.save(image);
        Long imageId = savedImage.getId();

        // when
        imageRepository.deleteById(imageId);

        // then
        assertThat(imageRepository.findById(imageId)).isEmpty();
    }

    @Test
    @DisplayName("여러 이미지 일괄 저장 테스트")
    void saveAllImages() throws IOException {
        // given
        String bulkImage1 = new ClassPathResource("images/test-image-2.png").getFile().getAbsolutePath();
        String bulkImage2 = new ClassPathResource("images/test-image-3.png").getFile().getAbsolutePath();
        String bulkImage3 = new ClassPathResource("images/test-image-4.png").getFile().getAbsolutePath();

        List<Image> images = List.of(
                Image.create(bulkImage1),
                Image.create(bulkImage2),
                Image.create(bulkImage3)
        );

        // when
        List<Image> savedImages = imageRepository.saveAll(images);

        // then
        assertThat(savedImages).hasSize(3);
        assertThat(savedImages).allMatch(image -> image.getId() != null);
        assertThat(savedImages).extracting(Image::getImageUrl)
                .contains(bulkImage1, bulkImage2, bulkImage3);
    }

    @Test
    @DisplayName("이미지 개수 조회 테스트")
    void countImages() throws IOException {
        // given
        String countImage1 = new ClassPathResource("images/test-image-5.png").getFile().getAbsolutePath();
        String countImage2 = new ClassPathResource("images/test-image-6.png").getFile().getAbsolutePath();

        Image image1 = Image.create(countImage1);
        Image image2 = Image.create(countImage2);
        imageRepository.saveAll(List.of(image1, image2));

        // when
        long count = imageRepository.count();

        // then
        assertThat(count).isEqualTo(4); // setUp에서 생성된 2개 + 새로 추가된 2개
    }
}
