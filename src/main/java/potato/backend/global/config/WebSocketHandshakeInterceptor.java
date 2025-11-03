package potato.backend.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import potato.backend.global.security.jwt.JwtUtil;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 핸드셰이크 인터셉터
 * WebSocket 연결 시점에 JWT 토큰을 검증하고 인증된 사용자 정보를 Principal로 설정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    /**
     * 핸드셰이크 전에 실행
     * JWT 토큰을 검증하고 인증 정보를 WebSocket 세션에 저장합니다.
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                     WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        // Authorization 헤더에서 JWT 토큰 추출
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            log.warn("WebSocket 핸드셰이크 실패: Authorization 헤더가 없습니다");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        String authHeader = authHeaders.get(0);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("WebSocket 핸드셰이크 실패: 유효하지 않은 Authorization 헤더 형식");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7); // "Bearer " 제거

        // JWT 토큰 검증
        if (!jwtUtil.validateToken(token)) {
            log.warn("WebSocket 핸드셰이크 실패: 유효하지 않은 JWT 토큰");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        // JWT에서 인증 정보 추출
        Authentication authentication = jwtUtil.getAuthentication(token);
        if (authentication == null || authentication.getPrincipal() == null) {
            log.warn("WebSocket 핸드셰이크 실패: JWT에서 인증 정보를 추출할 수 없습니다");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        // WebSocket 세션에 인증 정보 저장 (Principal)
        attributes.put("principal", authentication.getPrincipal());
        attributes.put("authentication", authentication);
        
        log.info("WebSocket 핸드셰이크 성공: memberId={}", 
                ((potato.backend.global.security.oauth.UserInfo) authentication.getPrincipal()).memberId());
        
        return true;
    }

    /**
     * 핸드셰이크 후에 실행
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 핸드셰이크 후 오류 발생", exception);
        }
    }
}

