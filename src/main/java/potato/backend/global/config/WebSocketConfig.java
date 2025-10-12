package potato.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // STOMP 기반 웹소켓 메시징을 활성화하는 스프링 설정
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String CHAT_ENDPOINT = "/ws-chat"; // 웹소켓으로 채팅방을 연결하는 엔드포인트
    private static final String APPLICATION_DESTINATION_PREFIX = "/app"; // 클라이언트가 서버로 메시지를 보내는 프리픽스
    private static final String SIMPLE_BROKER_PREFIX = "/topic"; // 서버의 브로드캐스트, 클라이언트가 구독할때 사용하는 프리픽스
    // 브로드 캐스트: 서버가 메시지를 보내면, 그 채널을 구독(듣고)하는 모든 클라이언트가 메시지를 받음.
    // 구독: 서버가 만든 채널을 구독하면 클라이언트가 메시지를 실시간으로 받음.

    // 클라이언트가 처음 STOMP 웹소켓 연결을 맺을때 엔드포인트를 등록하는 메서드
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(CHAT_ENDPOINT) // /ws-chat 으로 URL을 열어두면 클라이언트가 연결 가능
                .setAllowedOriginPatterns("*") // 어떤 도메인에서든 연결 허용
                .withSockJS(); // 브라우저가 웹소켓을 지원하지 않을때 AJAX의 SockJS 폴백 방식으로 통신
    }

    // 메시지 엔드포인트를 정하는 메서드
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(SIMPLE_BROKER_PREFIX); // 내장된 브로커를 켜서 서버가 /topic/** 으로 보내는 메시지를 브로드캐스트
        registry.setApplicationDestinationPrefixes(APPLICATION_DESTINATION_PREFIX); // 클라이언트가 서버로 메시지를 보낼때 /app을 프리픽스로 붙이도록 강제
    }
}
