package potato.backend.global.util;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final ObjectMapper objectMapper;

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (Objects.equals(name, cookie.getName())) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public static void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            String path,
            SameSite sameSite,
            String domain,
            long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path(path)
                .httpOnly(true)
                .secure(true)
                .sameSite(sameSite.attributeValue())
                .domain(domain)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static void deleteCookie(
            HttpServletRequest request, HttpServletResponse response, String name, String path) {
        ResponseCookie deleteCookie = ResponseCookie.from(name, "")
                .path(path)
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite(SameSite.NONE.attributeValue())
                .domain(UrlUtil.getRegistrableDomain(request.getServerName()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

    public String serialize(Object obj) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(obj);
            return Base64.getUrlEncoder().encodeToString(jsonBytes);
        } catch (Exception e) {
            log.error("[CookieUtil] 직렬화 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            String base64Value = cookie.getValue();
            byte[] jsonBytes = Base64.getUrlDecoder().decode(base64Value);
            return objectMapper.readValue(jsonBytes, cls);
        } catch (Exception e) {
            log.error("[CookieUtil] 역직렬화 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
