package potato.backend.domain.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import potato.backend.domain.chat.dto.chatMessage.ChatMessageReadRequest;
import potato.backend.domain.chat.dto.chatMessage.ChatMessageResponse;
import potato.backend.domain.chat.dto.chatMessage.ChatReadResponse;
import potato.backend.domain.chat.dto.chatMessage.ChatSendRequest;
import potato.backend.domain.chat.dto.chatMessage.ChatUnreadCountResponse;
import potato.backend.domain.chat.dto.chatRoom.ChatRoomReadRequest;
import potato.backend.domain.chat.service.ChatMessageService;

import java.util.Map;

/**
 * 채팅 메시지 컨트롤러
 */
@Controller
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/chat")
public class ChatMessageController {

    private static final String TOPIC_PREFIX = "/topic/chat/"; // 클라이언트가 구독하는 채널 주소 앞 부분

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate; // 스프링에 제공하는 메시지 전송 도구

    // TODO: Swagger 커스텀 문서화, 예외처리 응답 바디 추가
    /**
     * 메시지 전송/수신을 처리하는 메서드
     * @param roomId 채팅방 아이디
     * @param payload 메시지 전송 요청 정보
     * @return 메시지 전송 결과
     */
    @MessageMapping("/api/v1/chat/{roomId}")
    public void handleMessage(@DestinationVariable Long roomId, Map<String, Object> payload) {
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        String content = (String) payload.get("content");

        log.info("메시지 수신: roomId={}, senderId={}, content={}", roomId, senderId, content);

        ChatSendRequest request = ChatSendRequest.of(senderId, content);
        ChatMessageResponse response = chatMessageService.sendMessage(roomId, request);
        // 특정 경로로 메시지를 보내는 메서드
        // /topic/chat/{roomId} 경로로 response를 전송
        messagingTemplate.convertAndSend(TOPIC_PREFIX + roomId, response);
    }

    /**
     * 특정 메시지를 읽음 처리하는 API
     * @param messageId 읽음 처리할 메시지 ID
     * @param request 읽음 처리 요청 정보 (memberId 포함)
     * @return 읽음 처리 결과
     */
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ChatReadResponse> markMessageAsRead(
            @PathVariable Long messageId,
            @RequestBody ChatMessageReadRequest request) {

        log.info("메시지 읽음 처리 요청: messageId={}, memberId={}", messageId, request.getMemberId());

        try {
            ChatReadResponse readResponse = ChatReadResponse.ofMessage(messageId, request.getMemberId(), true);

            log.info("메시지 읽음 처리 완료: messageId={}", messageId);
            return ResponseEntity.ok(readResponse);

        } catch (Exception e) {
            log.error("메시지 읽음 처리 실패: messageId={}, error={}", messageId, e.getMessage());
            ChatReadResponse readResponse = ChatReadResponse.ofMessage(messageId, request.getMemberId(), false);
            return ResponseEntity.badRequest().body(readResponse);
        }
    }

    /**
     * 채팅방의 모든 메시지를 읽음 처리하는 API
     * @param roomId 채팅방 ID
     * @param request 읽음 처리 요청 정보 (memberId 포함)
     * @return 읽음 처리 결과
     */
    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<ChatReadResponse> markAllMessagesAsReadInRoom(
            @PathVariable Long roomId,
            @RequestBody ChatRoomReadRequest request) {

        log.info("채팅방 전체 메시지 읽음 처리 요청: roomId={}, memberId={}", roomId, request.getMemberId());

        try {
            int readCount = chatMessageService.markAllMessagesAsReadInRoom(roomId, request.getMemberId());
            ChatReadResponse readResponse = ChatReadResponse.ofRoom(roomId, request.getMemberId(), readCount, true);

            log.info("채팅방 전체 메시지 읽음 처리 완료: roomId={}, readCount={}", roomId, readCount);
            return ResponseEntity.ok(readResponse);

        } catch (Exception e) {
            log.error("채팅방 전체 메시지 읽음 처리 실패: roomId={}, error={}", roomId, e.getMessage());
            ChatReadResponse readResponse = ChatReadResponse.ofRoom(roomId, request.getMemberId(), 0, false);
            return ResponseEntity.badRequest().body(readResponse);
        }
    }

    /**
     * 사용자의 전체 읽지 않은 메시지 개수를 조회하는 API
     * @param memberId 사용자 ID
     * @return 읽지 않은 메시지 개수
     */
    @GetMapping("/messages/unread-count")
    public ResponseEntity<ChatUnreadCountResponse> getUnreadMessageCount(@RequestParam Long memberId) {
        log.info("읽지 않은 메시지 개수 조회 요청: memberId={}", memberId);

        try {
            long unreadCount = chatMessageService.getUnreadMessageCount(memberId);
            ChatUnreadCountResponse response = ChatUnreadCountResponse.ofTotal(memberId, unreadCount);

            log.info("읽지 않은 메시지 개수 조회 완료: memberId={}, count={}", memberId, unreadCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("읽지 않은 메시지 개수 조회 실패: memberId={}, error={}", memberId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 채팅방의 읽지 않은 메시지 개수를 조회하는 API
     * @param roomId 채팅방 ID
     * @param memberId 사용자 ID
     * @return 읽지 않은 메시지 개수
     */
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<ChatUnreadCountResponse> getUnreadMessageCountInRoom(
            @PathVariable Long roomId,
            @RequestParam Long memberId) {

        log.info("채팅방 읽지 않은 메시지 개수 조회 요청: roomId={}, memberId={}", roomId, memberId);

        try {
            long unreadCount = chatMessageService.getUnreadMessageCountInRoom(roomId, memberId);
            ChatUnreadCountResponse response = ChatUnreadCountResponse.ofRoom(memberId, roomId, unreadCount);

            log.info("채팅방 읽지 않은 메시지 개수 조회 완료: roomId={}, memberId={}, count={}", roomId, memberId, unreadCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("채팅방 읽지 않은 메시지 개수 조회 실패: roomId={}, memberId={}, error={}", roomId, memberId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
