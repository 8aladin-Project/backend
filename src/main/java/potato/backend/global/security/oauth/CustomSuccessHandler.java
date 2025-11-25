package potato.backend.global.security.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import potato.backend.global.security.jwt.JwtService;
import potato.backend.global.util.CookieUtil;
import potato.backend.global.util.UrlUtil;
import potato.backend.global.validator.URIValidator;

import static potato.backend.global.constant.SecurityConstant.*;
import static potato.backend.global.constant.UrlConstant.DEFAULT_CLIENT_URL;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);
        CookieUtil.deleteCookie(request, response, NEXT_URL_COOKIE_NAME, "/");

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        UserInfo userInfo = customOAuth2User.getUserInfo();
        
        // Access Token 생성
        String accessToken = jwtService.createAccessToken(userInfo);
        
        // Refresh Token 생성
        String refreshToken = jwtService.createRefreshToken(userInfo);

        // Refresh Token을 쿠키에 저장
        CookieUtil.addCookie(
                response,
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                "/api/v1/auth",
                SameSite.NONE,
                UrlUtil.getRegistrableDomain(request.getServerName()),
                REFRESH_TOKEN_EXPIRATION_SECONDS);

        // Access Token을 쿼리 파라미터로 전달 (프론트엔드에서 추출 후 즉시 제거)
        // 보안을 위해 짧은 시간만 유효하도록 설정되어 있음
        String redirectUrl = targetUrl;
        if (targetUrl.contains("?")) {
            redirectUrl += "&accessToken=" + accessToken;
        } else {
            redirectUrl += "?accessToken=" + accessToken;
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    @Override
    protected String determineTargetUrl(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> nextUrl =
                CookieUtil.getCookie(request, NEXT_URL_COOKIE_NAME).map(Cookie::getValue);

        if (nextUrl.isPresent()) {
            String targetUrl = nextUrl.get();
            URI uri;
            try {
                uri = new URI(targetUrl);
            } catch (URISyntaxException e) {
                return DEFAULT_CLIENT_URL;
            }
            if (URIValidator.isAllowedRedirectUri(uri)) {
                return targetUrl;
            } else {
                return DEFAULT_CLIENT_URL;
            }
        }

        return DEFAULT_CLIENT_URL;
    }
}
