package potato.backend.domain.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.category.repository.CategoryRepository;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.domain.Status;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Product Repository 테스트")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member testMember;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성
        testMember = Member.create("테스트유저", "test@example.com", "hashedPassword", "010-1234-5678");
        memberRepository.save(testMember);

        // 테스트용 카테고리 생성
        testCategory = Category.create("전자기기");
        categoryRepository.save(testCategory);
    }

    @Test
    @DisplayName("상품 생성 및 저장 테스트")
    void createAndSaveProduct() {
        // given
        List<String> imageUrls = List.of("image1.jpg", "image2.jpg");

        Product product = Product.create(
                testMember,
                List.of(testCategory),
                "아이폰 15 Pro",
                "새 제품입니다",
                imageUrls,
                BigDecimal.valueOf(1500000),
                Status.SELLING,
                "main-image.jpg"
        );

        // when
        Product savedProduct = productRepository.save(product);

        // then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getTitle()).isEqualTo("아이폰 15 Pro");
        assertThat(savedProduct.getPrice()).isEqualTo(BigDecimal.valueOf(1500000));
        assertThat(savedProduct.getStatus()).isEqualTo(Status.SELLING);
        assertThat(savedProduct.getImages()).hasSize(2);
    }

    @Test
    @DisplayName("상품 ID로 조회 테스트")
    void findById() {
        // given
        Product product = Product.create(
                testMember,
                List.of(testCategory),
                "맥북 프로",
                "고성능 노트북",
                List.of("image1.jpg"),
                BigDecimal.valueOf(2500000),
                Status.SELLING,
                "macbook.jpg"
        );
        Product savedProduct = productRepository.save(product);

        // when
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        // then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getTitle()).isEqualTo("맥북 프로");
    }

    @Test
    @DisplayName("모든 상품 페이징 조회 테스트")
    void findAllProducts() {
        // given
        for (int i = 1; i <= 5; i++) {
            Product product = Product.create(
                    testMember,
                    List.of(testCategory),
                    "상품 " + i,
                    "설명 " + i,
                    List.of("image" + i + ".jpg"),
                    BigDecimal.valueOf(10000 * i),
                    Status.SELLING,
                    "main" + i + ".jpg"
            );
            productRepository.save(product);
        }

        // when
        Pageable pageable = PageRequest.of(0, 3);
        Page<Product> productPage = productRepository.findAll(pageable);

        // then
        assertThat(productPage.getContent()).hasSize(3);
        assertThat(productPage.getTotalElements()).isEqualTo(5);
        assertThat(productPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("상품 업데이트 테스트")
    void updateProduct() {
        // given
        Product product = Product.create(
                testMember,
                List.of(testCategory),
                "원래 제목",
                "원래 내용",
                List.of("image1.jpg"),
                BigDecimal.valueOf(100000),
                Status.SELLING,
                "original.jpg"
        );
        Product savedProduct = productRepository.save(product);

        // when
        savedProduct.update("수정된 제목", "수정된 내용", BigDecimal.valueOf(150000), Status.SOLD_OUT, "updated.jpg");
        productRepository.save(savedProduct);

        // then
        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedProduct.getContent()).isEqualTo("수정된 내용");
        assertThat(updatedProduct.getPrice()).isEqualTo(BigDecimal.valueOf(150000));
        assertThat(updatedProduct.getStatus()).isEqualTo(Status.SOLD_OUT);
        assertThat(updatedProduct.getMainImageUrl()).isEqualTo("updated.jpg");
    }

    @Test
    @DisplayName("상품 삭제 테스트")
    void deleteProduct() {
        // given
        Product product = Product.create(
                testMember,
                List.of(testCategory),
                "삭제될 상품",
                "삭제 테스트",
                List.of("image1.jpg"),
                BigDecimal.valueOf(50000),
                Status.SELLING,
                "delete.jpg"
        );
        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();

        // when
        productRepository.deleteById(productId);

        // then
        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertThat(deletedProduct).isEmpty();
    }

    @Test
    @DisplayName("상품 존재 여부 확인 테스트")
    void existsById() {
        // given
        Product product = Product.create(
                testMember,
                List.of(testCategory),
                "존재 확인 상품",
                "테스트",
                List.of("image1.jpg"),
                BigDecimal.valueOf(30000),
                Status.SELLING,
                "exists.jpg"
        );
        Product savedProduct = productRepository.save(product);

        // when & then
        assertThat(productRepository.existsById(savedProduct.getId())).isTrue();
        assertThat(productRepository.existsById(99999L)).isFalse();
    }

    @Test
    @DisplayName("상품 개수 조회 테스트")
    void countProducts() {
        // given
        for (int i = 1; i <= 3; i++) {
            Product product = Product.create(
                    testMember,
                    List.of(testCategory),
                    "상품 " + i,
                    "설명 " + i,
                    List.of("image" + i + ".jpg"),
                    BigDecimal.valueOf(10000 * i),
                    Status.SELLING,
                    "main" + i + ".jpg"
            );
            productRepository.save(product);
        }

        // when
        long count = productRepository.count();

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("여러 상품 일괄 저장 테스트")
    void saveAllProducts() {
        // given
        List<Product> products = List.of(
                Product.create(
                        testMember,
                        List.of(testCategory),
                        "상품1",
                        "설명1",
                        List.of("image1.jpg"),
                        BigDecimal.valueOf(10000),
                        Status.SELLING,
                        "main1.jpg"
                ),
                Product.create(
                        testMember,
                        List.of(testCategory),
                        "상품2",
                        "설명2",
                        List.of("image2.jpg"),
                        BigDecimal.valueOf(20000),
                        Status.SELLING,
                        "main2.jpg"
                ),
                Product.create(
                        testMember,
                        List.of(testCategory),
                        "상품3",
                        "설명3",
                        List.of("image3.jpg"),
                        BigDecimal.valueOf(30000),
                        Status.SELLING,
                        "main3.jpg"
                )
        );

        // when
        List<Product> savedProducts = productRepository.saveAll(products);

        // then
        assertThat(savedProducts).hasSize(3);
        assertThat(savedProducts).allMatch(p -> p.getId() != null);
    }

    @Test
    @DisplayName("제목으로 상품 검색 테스트")
    void searchByTitle() {
        // given
        Product product1 = Product.create(testMember, List.of(testCategory), "아이폰 15", "설명1", List.of("image1.jpg"), BigDecimal.valueOf(1000000), Status.SELLING, "main1.jpg");
        Product product2 = Product.create(testMember, List.of(testCategory), "아이폰 14", "설명2", List.of("image2.jpg"), BigDecimal.valueOf(900000), Status.SELLING, "main2.jpg");
        Product product3 = Product.create(testMember, List.of(testCategory), "갤럭시 S24", "설명3", List.of("image3.jpg"), BigDecimal.valueOf(1100000), Status.SELLING, "main3.jpg");

        productRepository.saveAll(List.of(product1, product2, product3));

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> searchResult = productRepository.searchByTitle("아이폰", pageable);

        // then
        assertThat(searchResult.getContent()).hasSize(2);
        assertThat(searchResult.getContent()).extracting(Product::getTitle)
                .containsExactlyInAnyOrder("아이폰 15", "아이폰 14");
    }

    @Test
    @DisplayName("상태별 상품 조회 테스트")
    void findByStatus() {
        // given
        Product product1 = Product.create(testMember, List.of(testCategory), "판매중 상품1", "설명1", List.of("image1.jpg"), BigDecimal.valueOf(10000), Status.SELLING, "main1.jpg");
        Product product2 = Product.create(testMember, List.of(testCategory), "판매중 상품2", "설명2", List.of("image2.jpg"), BigDecimal.valueOf(20000), Status.SELLING, "main2.jpg");
        Product product3 = Product.create(testMember, List.of(testCategory), "품절 상품", "설명3", List.of("image3.jpg"), BigDecimal.valueOf(30000), Status.SOLD_OUT, "main3.jpg");

        productRepository.saveAll(List.of(product1, product2, product3));

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> sellingProducts = productRepository.findByStatus(Status.SELLING, pageable);
        Page<Product> soldOutProducts = productRepository.findByStatus(Status.SOLD_OUT, pageable);

        // then
        assertThat(sellingProducts.getContent()).hasSize(2);
        assertThat(soldOutProducts.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("가격 범위별 상품 조회 테스트")
    void findByPriceRange() {
        // given
        for (int i = 1; i <= 5; i++) {
            Product product = Product.create(
                    testMember,
                    List.of(testCategory),
                    "상품 " + i,
                    "설명 " + i,
                    List.of("image" + i + ".jpg"),
                    BigDecimal.valueOf(10000 * i),
                    Status.SELLING,
                    "main" + i + ".jpg"
            );
            productRepository.save(product);
        }

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> priceRangeProducts = productRepository.findByPriceRange(
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(40000),
                pageable
        );

        // then
        assertThat(priceRangeProducts.getContent()).hasSize(3);
        assertThat(priceRangeProducts.getContent()).allMatch(
                p -> p.getPrice().compareTo(BigDecimal.valueOf(20000)) >= 0
                        && p.getPrice().compareTo(BigDecimal.valueOf(40000)) <= 0
        );
    }

    @Test
    @DisplayName("회원별 상품 조회 테스트")
    void findByMemberId() {
        // given
        Member anotherMember = Member.create("다른유저", "another@example.com", "hashedPassword", "010-9876-5432");
        memberRepository.save(anotherMember);

        Product product1 = Product.create(testMember, List.of(testCategory), "테스트유저 상품1", "설명1", List.of("image1.jpg"), BigDecimal.valueOf(10000), Status.SELLING, "main1.jpg");
        Product product2 = Product.create(testMember, List.of(testCategory), "테스트유저 상품2", "설명2", List.of("image2.jpg"), BigDecimal.valueOf(20000), Status.SELLING, "main2.jpg");
        Product product3 = Product.create(anotherMember, List.of(testCategory), "다른유저 상품", "설명3", List.of("image3.jpg"), BigDecimal.valueOf(30000), Status.SELLING, "main3.jpg");

        productRepository.saveAll(List.of(product1, product2, product3));

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> testMemberProducts = productRepository.findByMemberId(testMember.getId(), pageable);

        // then
        assertThat(testMemberProducts.getContent()).hasSize(2);
        assertThat(testMemberProducts.getContent()).allMatch(
                p -> p.getMember().getId().equals(testMember.getId())
        );
    }

    @Test
    @DisplayName("상품 카테고리 관계 테스트")
    void productCategoryRelation() {
        // given
        Category category1 = Category.create("전자기기");
        Category category2 = Category.create("스마트폰");
        categoryRepository.saveAll(List.of(category1, category2));

        Product product = Product.create(
                testMember,
                List.of(category1, category2),
                "아이폰 15",
                "멀티 카테고리 상품",
                List.of("image1.jpg"),
                BigDecimal.valueOf(1500000),
                Status.SELLING,
                "main.jpg"
        );

        // when
        Product savedProduct = productRepository.save(product);

        // then
        assertThat(savedProduct.getCategories()).hasSize(2);
        assertThat(savedProduct.getCategories()).extracting(Category::getCategoryName)
                .containsExactlyInAnyOrder("전자기기", "스마트폰");
    }

    @Test
    @DisplayName("상품 이미지 관계 테스트")
    void productImageRelation() {
        // given
        Product product = Product.create(
                testMember,
                List.of(testCategory),
                "멀티 이미지 상품",
                "여러 이미지가 있는 상품",
                List.of("image1.jpg", "image2.jpg", "image3.jpg"),
                BigDecimal.valueOf(100000),
                Status.SELLING,
                "main.jpg"
        );

        // when
        Product savedProduct = productRepository.save(product);
        Product foundProduct = productRepository.findById(savedProduct.getId()).orElseThrow();

        // then
        assertThat(foundProduct.getImages()).hasSize(3);
        assertThat(foundProduct.getImages()).allMatch(img -> img.getProduct() != null);
    }
}
