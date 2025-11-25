package potato.backend.domain.product.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.category.repository.CategoryRepository;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.domain.Status;
import potato.backend.domain.product.domain.Condition;
import potato.backend.domain.product.dto.ProductCreateRequest;
import potato.backend.domain.product.dto.ProductListResponse;
import potato.backend.domain.product.dto.ProductResponse;
import potato.backend.domain.product.dto.ProductUpdateRequest;
import potato.backend.domain.product.exception.ProductNotFoundException;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.exception.MemberNotFoundException;
import potato.backend.domain.user.repository.MemberRepository;
import potato.backend.global.exception.CustomException;
import potato.backend.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    private final MemberRepository memberRepository;

    private final CategoryRepository categoryRepository;

    /**
     * 상품 생성
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("상품 생성 시작 - title: {}, price: {}, status: {}", 
                request.getTitle(), request.getPrice(), request.getStatus());
        
        // 카테고리 필수 검증
        if (request.getCategory() == null || request.getCategory().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_ARGUMENT, "상품 생성 시 카테고리는 최소 1개 이상이어야 합니다");
        }
        
        Member member = memberRepository.findById(request.getMemberId())
                 .orElseThrow(() -> new MemberNotFoundException(request.getMemberId()));

        // 입력된 카테고리명 정규화: trim, 공백 제거, 중복 제거
        List<String> requestedCategoryNames = request.getCategory().stream()
                .filter(name -> name != null)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();

        if (requestedCategoryNames.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_ARGUMENT, "유효한 카테고리명이 최소 1개 이상이어야 합니다");
        }

        // 1) 이미 존재하는 카테고리 조회
        List<Category> existingCategories = categoryRepository.findByNameIn(requestedCategoryNames);

        // 2) 없는 이름은 새로 생성
        java.util.Set<String> existingNames = existingCategories.stream()
                .map(Category::getCategoryName)
                .collect(java.util.stream.Collectors.toSet());

        List<String> missingNames = requestedCategoryNames.stream()
                .filter(name -> !existingNames.contains(name))
                .toList();

        List<Category> newCategories = missingNames.stream()
                .map(potato.backend.domain.category.domain.Category::create)
                .toList();

        if (!newCategories.isEmpty()) {
            newCategories = categoryRepository.saveAll(newCategories);
        }

        // 3) 최종 카테고리 목록 결합
        List<Category> categories = new java.util.ArrayList<>(existingCategories);
        categories.addAll(newCategories);

        // Product.create()가 이미지 URL 문자열을 받아서 내부에서 Image 엔티티 생성
        Product product = Product.create(
                member,
                categories,
                request.getTitle(),
                request.getContent(),
                request.getImages(),
                BigDecimal.valueOf(request.getPrice()),
                Status.valueOf(request.getStatus()),
                request.getMainImageUrl(),
                Condition.valueOf(request.getCondition())
        );

        Product savedProduct = productRepository.save(product);
        log.info("상품 생성 완료 - productId: {}, title: {}", savedProduct.getId(), savedProduct.getTitle());
        return ProductResponse.fromEntity(savedProduct);
    }

    /**
     * 상품 목록 조회 (페이징)
     */
    public Page<ProductListResponse> getProductList(Pageable pageable) {
        log.info("상품 목록 조회 시작 - page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> products = productRepository.findAll(pageable);
        log.info("상품 목록 조회 완료 - 총 {}개 상품 조회", products.getTotalElements());
        return products.map(ProductListResponse::fromEntity);
    }

    /**
     * 상품 단건 조회
     */
    public ProductResponse getProduct(Long productId) {
        log.info("상품 단건 조회 시작 - productId: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        log.info("상품 단건 조회 완료 - productId: {}, title: {}", productId, product.getTitle());
        return ProductResponse.fromEntity(product);
    }

    /**
     * 상품 수정
     */
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        log.info("상품 수정 시작 - productId: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 카테고리 처리
        List<Category> categories = null;
        if (request.getCategory() != null) {
            // 입력된 카테고리명 정규화
            List<String> requestedCategoryNames = request.getCategory().stream()
                    .filter(name -> name != null)
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .distinct()
                    .toList();

            if (!requestedCategoryNames.isEmpty()) {
                // 1) 이미 존재하는 카테고리 조회
                List<Category> existingCategories = categoryRepository.findByNameIn(requestedCategoryNames);

                // 2) 없는 이름은 새로 생성
                java.util.Set<String> existingNames = existingCategories.stream()
                        .map(Category::getCategoryName)
                        .collect(java.util.stream.Collectors.toSet());

                List<String> missingNames = requestedCategoryNames.stream()
                        .filter(name -> !existingNames.contains(name))
                        .toList();

                List<Category> newCategories = missingNames.stream()
                        .map(potato.backend.domain.category.domain.Category::create)
                        .toList();

                if (!newCategories.isEmpty()) {
                    newCategories = categoryRepository.saveAll(newCategories);
                }

                // 3) 최종 카테고리 목록 결합
                categories = new java.util.ArrayList<>(existingCategories);
                categories.addAll(newCategories);
            }
        }

        product.update(request.getTitle(), request.getContent(),
                request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : null,
                request.getStatus() != null ? Status.valueOf(request.getStatus()) : null,
                request.getMainImageUrl(),
                categories,
                request.getImageUrls());

        log.info("상품 수정 완료 - productId: {}", productId);
        return ProductResponse.fromEntity(product);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("상품 삭제 시작 - productId: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        productRepository.delete(product);
        log.info("상품 삭제 완료 - productId: {}", productId);
    }
}