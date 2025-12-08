package potato.backend.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import potato.backend.domain.product.dto.ProductListResponse;
import potato.backend.domain.user.dto.FcmTokenRequest;
import potato.backend.domain.user.dto.FcmTokenResponse;
import potato.backend.domain.user.dto.SignUpRequest;
import potato.backend.domain.user.dto.SignUpResponse;
import potato.backend.domain.user.service.MemberService;
import potato.backend.global.util.MemberUtil;

import java.util.List;

/**
 * 회원 관련 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(name = "Members", description = "회원 관리 API")
public class MemberController {

    private final MemberService memberService;
    private final MemberUtil memberUtil;

    /**
     * 회원가입 API
     */
    @Operation(summary = "회원가입 API", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SignUpResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검증 실패)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 이메일",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("회원가입 요청: email={}", request.getEmail());
        
        SignUpResponse response = memberService.signUp(request);
        log.info("회원가입 완료: memberId={}", response.getMemberId());
        
        return ResponseEntity.ok(response);
    }

    /**
     * FCM 토큰 등록 API
     * 클라이언트 앱에서 FCM 토큰을 받아서 서버에 등록합니다.
     */
    @Operation(summary = "FCM 토큰 등록 API", description = "사용자의 FCM 디바이스 토큰을 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "FCM 토큰 등록 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FcmTokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (토큰이 null이거나 빈 문자열)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PutMapping("/fcm-token")
    public ResponseEntity<FcmTokenResponse> registerFcmToken(
            @Valid @RequestBody FcmTokenRequest request) {
        
        Long authenticatedMemberId = memberUtil.getCurrentUser().memberId();
        log.info("FCM 토큰 등록 요청: memberId={}", authenticatedMemberId);

        memberService.updateFcmToken(authenticatedMemberId, request.getFcmToken());
        
        FcmTokenResponse response = FcmTokenResponse.of(authenticatedMemberId, request.getFcmToken());
        log.info("FCM 토큰 등록 완료: memberId={}", authenticatedMemberId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 마이페이지 - 판매내역 조회 API
     * 내가 등록한 상품 목록을 조회합니다.
     */
    @Operation(summary = "판매내역 조회", description = "내가 등록한 상품 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "판매내역 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/me/sales")
    public ResponseEntity<List<ProductListResponse>> getSalesHistory() {
        Long authenticatedMemberId = memberUtil.getCurrentUser().memberId();
        log.info("판매내역 조회 요청: memberId={}", authenticatedMemberId);

        List<ProductListResponse> salesHistory = memberService.getSalesHistory(authenticatedMemberId);
        log.info("판매내역 조회 완료: memberId={}, 상품 개수={}", authenticatedMemberId, salesHistory.size());

        return ResponseEntity.ok(salesHistory);
    }

    /**
     * 마이페이지 - 구매내역 조회 API
     * 내가 구매한 상품 목록을 조회합니다.
     */
    @Operation(summary = "구매내역 조회", description = "내가 구매한 상품 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "구매내역 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/me/purchases")
    public ResponseEntity<List<ProductListResponse>> getPurchaseHistory() {
        Long authenticatedMemberId = memberUtil.getCurrentUser().memberId();
        log.info("구매내역 조회 요청: memberId={}", authenticatedMemberId);

        List<ProductListResponse> purchaseHistory = memberService.getPurchaseHistory(authenticatedMemberId);
        log.info("구매내역 조회 완료: memberId={}, 상품 개수={}", authenticatedMemberId, purchaseHistory.size());

        return ResponseEntity.ok(purchaseHistory);
    }

    /**
     * 테스트용 - 판매내역 조회 API (memberId 파라미터로 받기)
     * 개발 환경에서만 사용하세요.
     */
    @Operation(summary = "판매내역 조회 (테스트용)", description = "memberId를 파라미터로 받아 판매내역을 조회합니다. 개발/테스트 전용입니다.")
    @GetMapping("/{memberId}/sales")
    public ResponseEntity<List<ProductListResponse>> getSalesHistoryForTest(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long memberId) {
        log.info("판매내역 조회 요청 (테스트용): memberId={}", memberId);

        List<ProductListResponse> salesHistory = memberService.getSalesHistory(memberId);
        log.info("판매내역 조회 완료 (테스트용): memberId={}, 상품 개수={}", memberId, salesHistory.size());

        return ResponseEntity.ok(salesHistory);
    }

    /**
     * 테스트용 - 구매내역 조회 API (memberId 파라미터로 받기)
     * 개발 환경에서만 사용하세요.
     */
    @Operation(summary = "구매내역 조회 (테스트용)", description = "memberId를 파라미터로 받아 구매내역을 조회합니다. 개발/테스트 전용입니다.")
    @GetMapping("/{memberId}/purchases")
    public ResponseEntity<List<ProductListResponse>> getPurchaseHistoryForTest(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long memberId) {
        log.info("구매내역 조회 요청 (테스트용): memberId={}", memberId);

        List<ProductListResponse> purchaseHistory = memberService.getPurchaseHistory(memberId);
        log.info("구매내역 조회 완료 (테스트용): memberId={}, 상품 개수={}", memberId, purchaseHistory.size());

        return ResponseEntity.ok(purchaseHistory);
    }
}

