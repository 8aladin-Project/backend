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
        String refreshToken = jwtService.createRefreshToken(userInfo);

        CookieUtil.addCookie(
                response,
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                "/auth",
                SameSite.NONE,
                UrlUtil.getRegistrableDomain(request.getServerName()),
                REFRESH_TOKEN_EXPIRATION_SECONDS);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
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
