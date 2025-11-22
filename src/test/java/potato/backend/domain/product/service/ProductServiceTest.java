package potato.backend.domain.product.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import potato.backend.domain.category.domain.Category;
import potato.backend.domain.category.repository.CategoryRepository;
import potato.backend.domain.product.domain.Condition;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.domain.Status;
import potato.backend.domain.product.dto.ProductCreateRequest;
import potato.backend.domain.product.dto.ProductResponse;
import potato.backend.domain.product.dto.ProductUpdateRequest;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;

@DataJpaTest
@Import(ProductService.class)
@DisplayName("ProductService 상품 생성 테스트")
class ProductServiceTest {

        @Autowired
        private ProductService productService;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Test
        @DisplayName("회원, 이미지, 카테고리를 포함한 상품 생성 성공")
        void createProduct_success() {
                // given
                Member seller = memberRepository.save(
                                Member.create("판매자", "seller@example.com", "hashed-password", "010-1111-2222"));

                ProductCreateRequest request = ProductCreateRequest.of(
                                seller.getId(),
                                "테스트 상품",
                                List.of("디지털", "모바일"),
                                "테스트용 상품 설명",
                                "main-image.jpg",
                                List.of("detail1.jpg", "detail2.jpg"),
                                150_000L,
                                Status.SELLING.name(),
                                Condition.NEW.name());

                // when
                ProductResponse response = productService.createProduct(request);

                // then
                assertThat(response.getProductId()).isNotNull();
                assertThat(response.getTitle()).isEqualTo("테스트 상품");
                assertThat(response.getImages()).hasSize(2);

                Product persisted = productRepository.findById(response.getProductId()).orElseThrow();
                assertThat(persisted.getMember().getId()).isEqualTo(seller.getId());
                assertThat(persisted.getCategories()).hasSize(2);
                assertThat(persisted.getCategories()).extracting(Category::getCategoryName)
                                .containsExactlyInAnyOrder("디지털", "모바일");
                assertThat(persisted.getImages()).hasSize(2);
                assertThat(persisted.getMainImageUrl()).isEqualTo("main-image.jpg");
                assertThat(persisted.getStatus()).isEqualTo(Status.SELLING);
                assertThat(categoryRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("상품 수정 성공 - 카테고리와 이미지 포함")
        void updateProduct_success() {
                // given
                Member seller = memberRepository.save(
                                Member.create("판매자", "seller@example.com", "hashed-password", "010-1111-2222"));

                ProductCreateRequest createRequest = ProductCreateRequest.of(
                                seller.getId(),
                                "원래 상품",
                                List.of("원래카테고리"),
                                "원래 설명",
                                "original-main.jpg",
                                List.of("original-detail.jpg"),
                                10000L,
                                Status.SELLING.name(),
                                "NEW");
                ProductResponse createdProduct = productService.createProduct(createRequest);

                ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
                                .title("수정된 상품")
                                .content("수정된 설명")
                                .price(20000L)
                                .status(Status.RESERVED.name())
                                .mainImageUrl("updated-main.jpg")
                                .imageUrls(List.of("updated-detail1.jpg", "updated-detail2.jpg"))
                                .category(List.of("수정된카테고리1", "수정된카테고리2"))
                                .build();

                // when
                ProductResponse updatedResponse = productService.updateProduct(createdProduct.getProductId(),
                                updateRequest);

                // then
                assertThat(updatedResponse.getTitle()).isEqualTo("수정된 상품");

                Product persisted = productRepository.findById(updatedResponse.getProductId()).orElseThrow();
                assertThat(persisted.getTitle()).isEqualTo("수정된 상품");
                assertThat(persisted.getContent()).isEqualTo("수정된 설명");
                assertThat(persisted.getPrice()).isEqualByComparingTo(java.math.BigDecimal.valueOf(20000));
                assertThat(persisted.getStatus()).isEqualTo(Status.RESERVED);
                assertThat(persisted.getMainImageUrl()).isEqualTo("updated-main.jpg");

                // 카테고리 검증
                assertThat(persisted.getCategories()).hasSize(2);
                assertThat(persisted.getCategories()).extracting(Category::getCategoryName)
                                .containsExactlyInAnyOrder("수정된카테고리1", "수정된카테고리2");

                // 이미지 검증
                assertThat(persisted.getImages()).hasSize(2);
                assertThat(persisted.getImages()).extracting(potato.backend.domain.image.domain.Image::getImageUrl)
                                .containsExactlyInAnyOrder("updated-detail1.jpg", "updated-detail2.jpg");
        }
}
