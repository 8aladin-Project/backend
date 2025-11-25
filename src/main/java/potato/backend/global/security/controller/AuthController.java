package potato.backend.global.security.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;
import potato.backend.global.exception.CustomException;
import potato.backend.global.exception.ErrorCode;
import potato.backend.global.security.jwt.JwtService;
import potato.backend.global.security.jwt.JwtUtil;
import potato.backend.global.security.jwt.RefreshToken;
import potato.backend.global.security.jwt.RefreshTokenRepository;
import potato.backend.global.security.oauth.UserInfo;
import potato.backend.global.util.CookieUtil;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static potato.backend.global.constant.SecurityConstant.REFRESH_TOKEN_COOKIE_NAME;

@Tag(name = "auth")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final Optional<ClientRegistrationRepository> clientRegistrationRepository;

    @PostMapping("/token/issue")
    @Operation(
            summary = "Access Token 발급",
            description = "Refresh Token을 사용하여 Access Token을 발급합니다.",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = AccessTokenResponse.class))),
                @ApiResponse(responseCode = "401", content = @Content),
            })
    public ResponseEntity<AccessTokenResponse> issueAccessToken(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookie,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (refreshTokenCookie == null || refreshTokenCookie.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 1. 리프레시 토큰 검증
        if (!jwtUtil.validateToken(refreshTokenCookie)) {
            CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME, "/api/v1/auth");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Claims claims;
        try {
            claims = jwtUtil.getClaims(refreshTokenCookie);
        } catch (Exception e) {
            CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME, "/api/v1/auth");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. memberId를 추출하고 리프레시 토큰 저장소에서 조회
        Long memberId;
        try {
            memberId = Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME, "/api/v1/auth");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<RefreshToken> storedTokenOpt = refreshTokenRepository.findById(memberId);

        // 3. 조회된 리프레시 토큰과 쿠키의 리프레시 토큰 비교
        if (storedTokenOpt.isEmpty() || !Objects.equals(storedTokenOpt.get().getToken(), refreshTokenCookie)) {
            storedTokenOpt.ifPresent(token -> refreshTokenRepository.deleteById(memberId));
            CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME, "/api/v1/auth");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 4. UserInfo 객체를 위해 Member 엔티티 조회
        Member member = memberRepository.findById(memberId).orElseThrow(() -> {
            refreshTokenRepository.deleteById(memberId);
            CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME, "/api/v1/auth");
            return new CustomException(
                    ErrorCode.MEMBER_NOT_FOUND, ErrorCode.MEMBER_NOT_FOUND.getMessage() + ": id=" + memberId);
        });

        // 5. UserInfo 객체 생성하고 새로운 액세스 토큰 생성
        UserInfo userInfo = UserInfo.from(member);
        String newAccessToken = jwtService.createAccessToken(userInfo);

        return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
    }

    @GetMapping("/providers")
    @Operation(
            summary = "지원하는 소셜 로그인 제공자 목록 조회",
            description = "현재 설정된 소셜 로그인 제공자 목록을 반환합니다. "
                    + "프론트엔드에서 소셜 로그인 버튼을 동적으로 생성할 때 사용할 수 있습니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "소셜 로그인 제공자 목록 반환"),
            })
    public ResponseEntity<Map<String, Object>> getAvailableProviders() {
        Map<String, Object> response = new HashMap<>();

        if (clientRegistrationRepository.isEmpty()) {
            response.put("providers", java.util.Collections.emptyList());
            response.put("message", "소셜 로그인이 설정되지 않았습니다.");
            return ResponseEntity.ok(response);
        }

        ClientRegistrationRepository repository = clientRegistrationRepository.get();
        Map<String, String> providers = new HashMap<>();

        // Google
        if (repository.findByRegistrationId("google") != null) {
            providers.put("google", "/oauth2/authorization/google");
        }

        // Naver
        if (repository.findByRegistrationId("naver") != null) {
            providers.put("naver", "/oauth2/authorization/naver");
        }

        // Kakao
        if (repository.findByRegistrationId("kakao") != null) {
            providers.put("kakao", "/oauth2/authorization/kakao");
        }

        response.put("providers", providers);
        return ResponseEntity.ok(response);
    }
}
