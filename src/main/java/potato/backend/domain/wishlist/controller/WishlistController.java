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

import java.util.HashMap;
import java.util.Map;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishlists")
@Tag(name = "Wishlist", description = "위시리스트 관리 API")
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * 위시리스트에 상품 추가
     * @param memberId 회원 ID
     * @param productId 상품 ID
     */
    @Operation(summary = "위시리스트 추가", description = "상품을 위시리스트에 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "추가 성공"),
            @ApiResponse(responseCode = "404", description = "회원 또는 상품을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 위시리스트에 추가된 상품")
    })
    @PostMapping
    public ResponseEntity<Map<String, String>> addToWishlist(
            @Parameter(description = "회원 ID")
            @RequestParam Long memberId,
            @Parameter(description = "상품 ID")
            @RequestParam Long productId) {
        
        wishlistService.addToWishlist(memberId, productId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "상품이 위시리스트에 추가되었습니다");
        response.put("memberId", memberId.toString());
        response.put("productId", productId.toString());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 위시리스트에서 상품 제거
     * @param memberId 회원 ID
     * @param productId 상품 ID
     */
    @Operation(summary = "위시리스트 제거", description = "상품을 위시리스트에서 제거합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "제거 성공"),
            @ApiResponse(responseCode = "404", description = "회원, 상품 또는 위시리스트를 찾을 수 없음")
    })
    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeFromWishlist(
            @Parameter(description = "회원 ID")
            @RequestParam Long memberId,
            @Parameter(description = "상품 ID")
            @RequestParam Long productId) {
        
        wishlistService.removeFromWishlist(memberId, productId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "상품이 위시리스트에서 제거되었습니다");
        response.put("memberId", memberId.toString());
        response.put("productId", productId.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 위시리스트에 상품이 있는지 확인
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return true: 있음, false: 없음
     */
    @Operation(summary = "위시리스트 확인", description = "특정 상품이 위시리스트에 있는지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "확인 성공")
    @GetMapping("/check")
    public ResponseEntity<Boolean> isInWishlist(
            @Parameter(description = "회원 ID")
            @RequestParam Long memberId,
            @Parameter(description = "상품 ID")
            @RequestParam Long productId) {
        
        boolean isInWishlist = wishlistService.isInWishlist(memberId, productId);
        return ResponseEntity.ok(isInWishlist);
    }

}
