package potato.backend.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import potato.backend.domain.user.dto.FcmTokenRequest;
import potato.backend.domain.user.dto.FcmTokenResponse;
import potato.backend.domain.user.service.MemberService;
import potato.backend.global.util.MemberUtil;

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
}

