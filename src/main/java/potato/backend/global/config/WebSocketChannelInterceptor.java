package potato.backend.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import potato.backend.global.security.jwt.JwtUtil;

import java.security.Principal;
import java.util.List;

/**
 * WebSocket STOMP Channel Interceptor
 * STOMP 연결 시 JWT 토큰을 검증하고 Principal을 설정합니다.
 * 핸드셰이크 인터셉터에서 설정한 인증 정보를 STOMP 메시지에서 사용할 수 있도록 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // STOMP CONNECT 메시지 처리
            String token = extractTokenFromHeaders(accessor);
            
            if (token != null && jwtUtil.validateToken(token)) {
                org.springframework.security.core.Authentication authentication = jwtUtil.getAuthentication(token);
                if (authentication != null && authentication.getPrincipal() != null) {
                    // Principal 설정 (UserInfo가 Principal을 구현함)
                    Object principalObj = authentication.getPrincipal();
                    if (principalObj instanceof Principal principal) {
                        accessor.setUser(principal);
                        
                        if (principal instanceof potato.backend.global.security.oauth.UserInfo userInfo) {
                            log.info("STOMP 연결 인증 성공: memberId={}", userInfo.memberId());
                        } else {
                            log.info("STOMP 연결 인증 성공: principal={}", principal.getName());
                        }
                    } else {
                        log.warn("Principal 타입이 아닙니다: {}", principalObj);
                        return null; // 연결 거부
                    }
                } else {
                    log.warn("STOMP 연결 실패: JWT에서 인증 정보를 추출할 수 없습니다");
                    return null; // 연결 거부
                }
            } else {
                log.warn("STOMP 연결 실패: 유효하지 않은 JWT 토큰");
                return null; // 연결 거부
            }
        }
        
        return message;
    }

    /**
     * STOMP CONNECT 헤더에서 JWT 토큰 추출
     * 클라이언트가 연결 시 헤더에 "Authorization: Bearer {token}" 형식으로 전송
     */
    private String extractTokenFromHeaders(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }
}

