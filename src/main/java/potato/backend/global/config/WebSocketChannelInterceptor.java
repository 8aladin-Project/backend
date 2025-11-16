package potato.backend.global.config;

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

import java.security.Principal;

/**
 * WebSocket STOMP Channel Interceptor
 * 핸드셰이크 인터셉터에서 설정한 Principal을 STOMP 세션에 설정합니다.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 핸드셰이크 인터셉터에서 설정한 Principal 가져오기
            Object principal = accessor.getSessionAttributes().get("principal");
            
            if (principal instanceof Principal) {
                accessor.setUser((Principal) principal);
                
                if (principal instanceof potato.backend.global.security.oauth.UserInfo userInfo) {
                    log.info("STOMP 연결 성공: memberId={}", userInfo.memberId());
                } else {
                    log.info("STOMP 연결 성공: principal={}", ((Principal) principal).getName());
                }
            } else {
                log.warn("STOMP 연결 실패: Principal이 설정되지 않았습니다");
                return null; // 연결 거부
            }
        }
        
        return message;
    }
}

