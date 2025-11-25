package potato.backend.global.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import potato.backend.global.security.dto.LoginRequest;
import potato.backend.global.security.dto.LoginResponse;
import potato.backend.global.security.jwt.JwtService;
import potato.backend.global.security.jwt.JwtUtil;
import potato.backend.global.security.jwt.RefreshTokenRepository;
import potato.backend.global.security.oauth.UserInfo;
import potato.backend.global.util.CookieUtil;
import potato.backend.global.util.UrlUtil;

import org.springframework.boot.web.server.Cookie.SameSite;

import static potato.backend.global.constant.SecurityConstant.REFRESH_TOKEN_COOKIE_NAME;
import static potato.backend.global.constant.SecurityConstant.REFRESH_TOKEN_EXPIRATION_SECONDS;

@Tag(name = "login")
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoginController {

    private final JwtService jwtService;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    @Operation(
            summary = "일반 로그인",
            description = "이메일과 비밀번호를 사용하여 로그인합니다.",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = LoginResponse.class))),
                @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않습니다", content = @Content),
            })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN_CREDENTIALS));

        // 2. 비밀번호가 없는 경우 (OAuth 회원)
        if (member.getHashedPassword() == null || member.getHashedPassword().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getHashedPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        // 4. UserInfo 객체 생성
        UserInfo userInfo = UserInfo.from(member);

        // 5. JWT 토큰 생성
        String accessToken = jwtService.createAccessToken(userInfo);
        String refreshToken = jwtService.createRefreshToken(userInfo);

        // 6. Refresh Token을 쿠키에 저장
        CookieUtil.addCookie(
                response,
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                "/api/v1/auth",
                SameSite.NONE,
                UrlUtil.getRegistrableDomain(request.getServerName()),
                REFRESH_TOKEN_EXPIRATION_SECONDS);

        // 7. 응답 반환
        LoginResponse loginResponse = new LoginResponse(
                accessToken,
                member.getId(),
                member.getName(),
                member.getEmail());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 삭제하고 로그아웃합니다. (일반 로그인 및 소셜 로그인 공통)",
            responses = {@ApiResponse(responseCode = "200", description = "로그아웃 성공")})
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookie,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (refreshTokenCookie != null && !refreshTokenCookie.isEmpty()) {
            Claims claims;
            try {
                claims = jwtUtil.getClaims(refreshTokenCookie);
                Long memberId = Long.parseLong(claims.getSubject());
                refreshTokenRepository.deleteById(memberId);
            } catch (Exception ignored) {
            }
        }

        // 리프레시 토큰 쿠키 삭제
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME, "/api/v1/auth");

        // 스프링 시큐리티 컨텍스트 초기화
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }
}

