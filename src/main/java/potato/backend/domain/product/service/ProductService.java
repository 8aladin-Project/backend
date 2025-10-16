package potato.backend.domain.product.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import potato.backend.domain.category.domain.Category;
import potato.backend.domain.category.repository.CategoryRepository;
import potato.backend.domain.image.domain.Image;
import potato.backend.domain.image.repository.ImageRepository;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.domain.Status;
import potato.backend.domain.product.dto.ProductCreateRequest;
import potato.backend.domain.product.dto.ProductListResponse;
import potato.backend.domain.product.dto.ProductResponse;
import potato.backend.domain.product.dto.ProductUpdateRequest;
import potato.backend.domain.product.exception.ProductNotFoundException;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.exception.MemberNotFoundException;
import potato.backend.domain.user.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    private final MemberRepository memberRepository;

    private final CategoryRepository categoryRepository;

    private  final ImageRepository imageRepository;

    /**
     * 상품 생성
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("상품 생성 시작 - title: {}, price: {}, status: {}", 
                request.getTitle(), request.getPrice(), request.getStatus());
        
        // TODO: Member 조회 로직 추가 (MemberRepository 필요)
        Member member = memberRepository.findById(request.getMemberId())
                 .orElseThrow(() -> new MemberNotFoundException(request.getMemberId()));

        List<Category> categories = categoryRepository.findByNameIn(request.getCategory());


        List<Image> images = new ArrayList<>();

        Product product = Product.create(
                member,
                categories,
                request.getTitle(),
                request.getContent(),
                images,
                BigDecimal.valueOf(request.getPrice()),
                Status.valueOf(request.getStatus()),
                request.getMainImageUrl()
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

        product.update(request.getTitle(), request.getContent(),
                BigDecimal.valueOf(request.getPrice()),
                Status.valueOf(request.getStatus()),
                request.getMainImageUrl());

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