package potato.backend.domain.wishlist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import potato.backend.domain.wishlist.service.WishlistService;
import potato.backend.domain.wishlist.dto.WishlistResponse;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishlists")
@Tag(name = "Wishlist", description = "위시리스트 관리 API")
public class WishlistController {

    private final WishlistService wishlistService;

    // 위시리스트 추가
    @Operation(summary = "위시리스트 추가", description = "상품을 위시리스트에 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "추가 성공"),
            @ApiResponse(responseCode = "404", description = "회원 또는 상품을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 위시리스트에 추가된 상품")
    })
    @PostMapping("/members/{memberId}/products/{productId}")
    public ResponseEntity<WishlistResponse> addToWishlist(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long memberId,
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Long productId) {
        
        wishlistService.addToWishlist(memberId, productId);
        
        WishlistResponse response = WishlistResponse.of(
            "상품이 위시리스트에 추가되었습니다", 
            memberId, 
            productId
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 위시리스트 제거
    @Operation(summary = "위시리스트 제거", description = "상품을 위시리스트에서 제거합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "제거 성공"),
            @ApiResponse(responseCode = "404", description = "회원, 상품 또는 위시리스트를 찾을 수 없음")
    })
    @DeleteMapping("/members/{memberId}/products/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long memberId,
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Long productId) {
        
        wishlistService.removeFromWishlist(memberId, productId);
        
        return ResponseEntity.noContent().build();
    }

    // 위시리스트 확인
    @Operation(summary = "위시리스트 확인", description = "특정 상품이 위시리스트에 있는지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "확인 성공")
    @GetMapping("/members/{memberId}/products/{productId}")
    public ResponseEntity<Boolean> isInWishlist(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long memberId,
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Long productId) {
        
        boolean isInWishlist = wishlistService.isInWishlist(memberId, productId);
        return ResponseEntity.ok(isInWishlist);
    }

}
