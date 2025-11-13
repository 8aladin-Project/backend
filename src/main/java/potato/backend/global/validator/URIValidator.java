package potato.backend.global.validator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import potato.backend.global.constant.UrlConstant;

@Slf4j
public class URIValidator {

    /**
     * 주어진 redirectUrl이 허용된 클라이언트 URL 목록에 있는 URL 중 하나와
     * 동일한 Origin(Scheme, Host, Port)을 갖는지 확인합니다.
     * Open Redirect 취약점 방지를 위해 사용됩니다.
     *
     * @param redirectUri 검증할 리다이렉트 URI
     * @return Origin이 허용 목록에 포함되면 true, 그렇지 않으면 false
     */
    public static boolean isAllowedRedirectUri(URI redirectUri) {

        // 2. 기본 URL 형식 검사 (Scheme과 Host가 없는 경우)
        // 예: "/some/path", "javascript:alert(1)" 등 방지
        if (redirectUri.getScheme() == null || redirectUri.getHost() == null) {
            return false;
        }

        // 3. Scheme이 http 또는 https인지 확인
        String scheme = redirectUri.getScheme().toLowerCase();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            return false;
        }

        // 4. 허용된 URL 목록과 Origin 비교
        for (String allowedUrl : UrlConstant.ALLOWED_CLIENT_URLS) {
            try {
                URI allowedUri = new URI(allowedUrl);

                // Scheme 비교 (대소문자 구분 안 함)
                if (!Objects.equals(
                        redirectUri.getScheme().toLowerCase(),
                        allowedUri.getScheme().toLowerCase())) {
                    continue; // Scheme 불일치
                }

                // Host 비교 (대소문자 구분 안 함)
                if (!Objects.equals(
                        redirectUri.getHost().toLowerCase(),
                        allowedUri.getHost().toLowerCase())) {
                    continue; // Host 불일치
                }

                // Port 비교 (기본 포트 처리 포함)
                int redirectPort = redirectUri.getPort();
                int allowedPort = allowedUri.getPort();

                // URI.getPort()는 포트가 명시되지 않으면 -1을 반환합니다. 기본 포트를 설정합니다.
                if (redirectPort == -1) {
                    redirectPort = getDefaultPort(redirectUri.getScheme());
                }
                if (allowedPort == -1) {
                    allowedPort = getDefaultPort(allowedUri.getScheme());
                }

                // 최종 포트 비교
                if (redirectPort == allowedPort) {
                    // Scheme, Host, Port가 모두 일치하면 허용
                    return true;
                }

            } catch (URISyntaxException e) {
                log.warn("[URIValidator] ALLOWED_CLIENT_URLS에 잘못된 형식의 URL이 포함되어 있습니다: allowedUrl={}", allowedUrl, e);
            }
        }

        // 허용 목록의 어떤 URL과도 Origin이 일치하지 않음
        return false;
    }

    private static int getDefaultPort(String scheme) {
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        } else if ("http".equalsIgnoreCase(scheme)) {
            return 80;
        }
        return -1; // 그 외의 Scheme은 기본 포트를 알 수 없음 (이미 isAllowedRedirectUrl에서 걸러짐)
    }
}
