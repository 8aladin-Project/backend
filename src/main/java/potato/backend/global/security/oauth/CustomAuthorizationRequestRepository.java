package potato.backend.global.security.oauth;

import java.net.URI;
import java.net.URISyntaxException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import potato.backend.global.util.CookieUtil;
import potato.backend.global.util.UrlUtil;
import potato.backend.global.validator.URIValidator;

import static potato.backend.global.constant.SecurityConstant.*;
import static potato.backend.global.constant.UrlConstant.DEFAULT_CLIENT_URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final CookieUtil cookieUtil;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtil.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> cookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }

        String nextParam = request.getParameter(NEXT_PARAM);
        if (nextParam == null) {
            nextParam = DEFAULT_CLIENT_URL;
        }

        URI nextUrl;
        try {
            nextUrl = new URI(nextParam);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (!URIValidator.isAllowedRedirectUri(nextUrl)) {
            return;
        }

        CookieUtil.addCookie(
                response,
                NEXT_URL_COOKIE_NAME,
                nextUrl.toString(),
                "/",
                SameSite.NONE,
                UrlUtil.getRegistrableDomain(request.getServerName()),
                OAUTH2_TOKEN_EXPIRATION_SECONDS);

        CookieUtil.addCookie(
                response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                cookieUtil.serialize(authorizationRequest),
                "/",
                SameSite.NONE,
                UrlUtil.getRegistrableDomain(request.getServerName()),
                OAUTH2_TOKEN_EXPIRATION_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest originalRequest = this.loadAuthorizationRequest(request);

        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, "/");

        return originalRequest;
    }
}
