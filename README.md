# Backend

## Getting Started - API 스펙 확인하기

- `./gradlew bootRun`으로 애플리케이션을 실행합니다 (기본 포트: 8080).
- 실행 후 `http://localhost:8080/swagger-ui/index.html`에서 REST API 명세를 확인할 수 있습니다.

## WebSocket Messaging

STOMP 통신이라 swagger로 문서화할 수 없어 따로 read에 적어두었습니다.
채팅 메시지는 STOMP 기반 WebSocket으로 주고받습니다.

1. `/ws-chat` 엔드포인트로 SockJS/STOMP 연결을 맺습니다.
2. 클라이언트는 `/app/api/v1/chat/{roomId}` 목적지로 메시지를 전송합니다. 페이로드는 `sender_id`, `content` 필드를 포함해야 합니다.
3. 서버는 `/topic/chat/{roomId}`를 구독 중인 모든 클라이언트에게 `ChatMessageResponse`를 브로드캐스트합니다.
