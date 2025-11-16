package potato.backend.domain.product.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import potato.backend.domain.product.dto.ProductCreateRequest;
import potato.backend.domain.product.dto.ProductListResponse;
import potato.backend.domain.product.dto.ProductResponse;
import potato.backend.domain.product.dto.ProductUpdateRequest;
import potato.backend.domain.product.service.ProductService;

@Tag(name = "Product", description = "상품 관리 API")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회 (페이징)
     */
    @Operation(
            summary = "상품 목록 조회",
            description = "페이징된 상품 목록을 조회합니다. page/size/sort 파라미터를 쿼리스트링으로 전달하세요. 예) ?page=0&size=20&sort=price,asc&sort=createdAt,desc"
    )
    @GetMapping
    public ResponseEntity<Page<ProductListResponse>> getProductList(
            @Parameter(description = "Pageable 쿼리 파라미터(page, size, sort=필드,방향)")
            @PageableDefault(size = 20) @ParameterObject Pageable pageable
    ) {
        log.info("상품 목록 조회 요청 - page: {}, size: {}, sort: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<ProductListResponse> products = productService.getProductList(pageable);
        log.info("상품 목록 조회 완료 - 총 {}개 상품 중 {}개 조회", 
                products.getTotalElements(), products.getNumberOfElements());
        return ResponseEntity.ok(products);
    }

    /**
     * 상품 상세 조회
     */
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Long productId
    ) {
        log.info("상품 상세 조회 요청 - productId: {}", productId);
        ProductResponse product = productService.getProduct(productId);
        log.info("상품 상세 조회 완료 - productId: {}, title: {}", productId, product.getTitle());
        return ResponseEntity.ok(product);
    }

    /**
     * 상품 생성
     */
    @Operation(summary = "상품 생성", description = "새로운 상품을 등록합니다.")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "상품 생성 정보", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "상품 생성 요청 예시",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "상품 생성 예시",
                                    value = "{\n  \"member_id\": 1,\n  \"title\": \"어쩌구\",\n  \"category\": [\"디지털\"],\n  \"content\": \"상세 설명\",\n  \"main_image_url\": \"https://api-dev-minio.8aladin.shop/paladin/images/main.png\",\n  \"images\": [\"https://api-dev-minio.8aladin.shop/paladin/images/detail.png\"],\n  \"price\": 3000,\n  \"status\": \"SELLING\"\n}"
                            )
                    )
            )
            @RequestBody ProductCreateRequest request
    ) {
        log.info("상품 생성 요청");
        ProductResponse product = productService.createProduct(request);
        log.info("상품 생성 완료 - productId: {}, title: {}", product.getProductId(), product.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * 상품 수정
     */
    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Long productId,
            @Parameter(description = "상품 수정 정보", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "상품 수정 요청 예시",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "상품 수정 예시",
                                    value = "{\n  \"title\": \"어쩌구-수정\",\n  \"content\": \"수정된 상세 설명\",\n  \"main_image_url\": \"https://api-dev-minio.8aladin.shop/paladin/images/main.png\",\n  \"image_urls\": [\"https://api-dev-minio.8aladin.shop/paladin/images/detail.png\"],\n  \"price\": 3500,\n  \"status\": \"SELLING\"\n}"
                            )
                    )
            )
            @RequestBody ProductUpdateRequest request
    ) {
        log.info("상품 수정 요청 - productId: {}", productId);
        ProductResponse product = productService.updateProduct(productId, request);
        log.info("상품 수정 완료 - productId: {}, title: {}", product.getProductId(), product.getTitle());
        return ResponseEntity.ok(product);
    }

    /**
     * 상품 삭제
     */
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Long productId
    ) {
        log.info("상품 삭제 요청 - productId: {}", productId);
        productService.deleteProduct(productId);
        log.info("상품 삭제 완료 - productId: {}", productId);
        return ResponseEntity.noContent().build();
    }
}
