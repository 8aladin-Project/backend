package potato.backend.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import potato.backend.domain.chat.service.ChatSessionManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebSocket 이벤트 리스너
 * WebSocket 연결/구독/해제 이벤트를 감지하여 ChatSessionManager에 반영합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatSessionManager chatSessionManager;
    
    // /topic/chat/{roomId} 패턴에서 roomId 추출을 위한 정규식
    private static final Pattern CHAT_ROOM_PATTERN = Pattern.compile("/topic/chat/(\\d+)");

    /**
     * WebSocket 연결 이벤트 (실제로는 STOMP CONNECT)
     * 연결만 하고 구독하지 않은 경우는 세션에 추가하지 않음
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.debug("WebSocket 연결됨: sessionId={}", sessionId);
    }

    /**
     * 특정 채팅방을 구독할 때 호출
     * /topic/chat/{roomId} 경로를 구독하면 해당 채팅방에 사용자가 연결된 것으로 간주
     */
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        Long memberId = extractMemberIdFromPrincipal(headerAccessor);
        
        if (destination != null && memberId != null) {
            Long roomId = extractRoomIdFromDestination(destination);
            if (roomId != null) {
                chatSessionManager.addSession(roomId, memberId);
                log.info("채팅방 구독: roomId={}, memberId={}, destination={}", roomId, memberId, destination);
            }
        } else {
            log.warn("채팅방 구독 실패: destination={}, memberId={}", destination, memberId);
        }
    }

    /**
     * 채팅방 구독 해제 시 호출
     */
    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        Long memberId = extractMemberIdFromPrincipal(headerAccessor);
        
        if (destination != null && memberId != null) {
            Long roomId = extractRoomIdFromDestination(destination);
            if (roomId != null) {
                chatSessionManager.removeSession(roomId, memberId);
                log.info("채팅방 구독 해제: roomId={}, memberId={}, destination={}", roomId, memberId, destination);
            }
        }
    }

    /**
     * WebSocket 연결 종료 시 호출
     * 사용자가 모든 채팅방에서 연결 해제된 것으로 처리
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long memberId = extractMemberIdFromPrincipal(headerAccessor);
        
        if (memberId != null) {
            chatSessionManager.removeAllSessions(memberId);
            log.info("WebSocket 연결 종료: memberId={}", memberId);
        }
    }

    /**
     * WebSocket 세션의 Principal에서 memberId 추출
     * 보안: 클라이언트가 보낸 헤더 값을 신뢰하지 않고, 서버가 인증한 Principal에서 memberId를 추출합니다.
     */
    private Long extractMemberIdFromPrincipal(StompHeaderAccessor headerAccessor) {
        try {
            Object principal = headerAccessor.getUser();
            if (principal instanceof potato.backend.global.security.oauth.UserInfo userInfo) {
                return userInfo.memberId();
            }
            log.warn("Principal이 UserInfo 타입이 아닙니다: {}", principal);
        } catch (Exception e) {
            log.warn("Principal에서 memberId 추출 실패", e);
        }
        return null;
    }

    /**
     * 구독 경로에서 roomId 추출
     * /topic/chat/{roomId} 형식에서 roomId를 추출
     */
    private Long extractRoomIdFromDestination(String destination) {
        if (destination == null) {
            return null;
        }
        
        Matcher matcher = CHAT_ROOM_PATTERN.matcher(destination);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("유효하지 않은 roomId: {}", destination);
            }
        }
        return null;
    }
}

