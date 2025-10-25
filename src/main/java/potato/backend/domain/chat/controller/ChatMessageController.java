package potato.backend.domain.chat.controller;

import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import potato.backend.domain.chat.dto.chatMessage.ChatMessageReadRequest;
import potato.backend.domain.chat.dto.chatMessage.ChatMessageResponse;
import potato.backend.domain.chat.dto.chatMessage.ChatReadResponse;
import potato.backend.domain.chat.dto.chatMessage.ChatSendRequest;
import potato.backend.domain.chat.dto.chatMessage.ChatUnreadCountResponse;
import potato.backend.domain.chat.dto.chatRoom.ChatRoomReadRequest;
import potato.backend.domain.chat.service.ChatMessageService;
import potato.backend.global.util.MemberUtil;

import jakarta.validation.Valid;
import java.util.Map;
import potato.backend.global.exception.ErrorResponse;

/**
 * 채팅 메시지 컨트롤러
 */
@Controller
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/chat")
@Tag(name = "ChatMessages", description = "채팅 메시지 관리 API")
public class ChatMessageController {

    private static final String TOPIC_PREFIX = "/topic/chat/"; // 클라이언트가 구독하는 채널 주소 앞 부분

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate; // 스프링에 제공하는 메시지 전송 도구
    private final MemberUtil memberUtil;

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
     * @param request 읽음 처리 요청 정보
     * @return 읽음 처리 결과
     */
    @Operation(summary = "메시지 읽음 처리 API", description = "특정 메시지를 읽음 처리합니다.")
    @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "읽음 처리 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatReadResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (요청 값 검증 실패)",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        name = "INVALID_ARGUMENT",
                        value = "{\"errorCodeName\":\"INVALID_ARGUMENT\",\"errorMessage\":\"유효하지 않은 인자입니다\"}"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "메시지를 찾을 수 없음 (CHAT_MESSAGE_NOT_FOUND)",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        name = "CHAT_MESSAGE_NOT_FOUND",
                        value = "{\"errorCodeName\":\"CHAT_MESSAGE_NOT_FOUND\",\"errorMessage\":\"메시지를 찾을 수 없습니다\"}"
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(
                        name = "INTERNAL_SERVER_ERROR",
                        value = "{\"errorCodeName\":\"INTERNAL_SERVER_ERROR\",\"errorMessage\":\"서버 내부 오류입니다\"}"
                    )
                )
            )
    })
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ChatReadResponse> markMessageAsRead(
            @Parameter(description = "읽음 처리할 메시지 ID", required = true)
            @PathVariable Long messageId,
            @Valid @RequestBody ChatMessageReadRequest request) {

        Long authenticatedMemberId = memberUtil.getCurrentUser().memberId();
        log.info("메시지 읽음 처리 요청: messageId={}, authenticatedMemberId={}", messageId, authenticatedMemberId);

        try {
            ChatMessageResponse messageResponse = chatMessageService.markMessageAsRead(messageId, authenticatedMemberId);
            ChatReadResponse readResponse = ChatReadResponse.ofMessage(
                messageResponse.getMessageId(),
                authenticatedMemberId,
                messageResponse.isRead()
            );

            log.info("메시지 읽음 처리 완료: messageId={}", messageId);
            return ResponseEntity.ok(readResponse);

        } catch (Exception e) {
            log.error("메시지 읽음 처리 실패: messageId={}, error={}", messageId, e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리하도록 예외를 다시 throw
        }
    }

    /**
     * 채팅방의 모든 메시지를 읽음 처리하는 API
     * @param roomId 채팅방 ID
     * @param request 읽음 처리 요청 정보
     * @return 읽음 처리 결과
     */
    @Operation(summary = "채팅방 전체 메시지 읽음 처리 API", description = "채팅방의 모든 메시지를 읽음 처리합니다.")
    @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "읽음 처리 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatReadResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (요청 값 검증 실패)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "INVALID_ARGUMENT",
                                value = "{\"errorCodeName\":\"INVALID_ARGUMENT\",\"errorMessage\":\"유효하지 않은 인자입니다\"}"
                        )
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "채팅방 접근 권한 없음 (CHAT_PARTICIPANT_NOT_FOUND)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "CHAT_PARTICIPANT_NOT_FOUND",
                                value = "{\"errorCodeName\":\"CHAT_PARTICIPANT_NOT_FOUND\",\"errorMessage\":\"채팅방에 참여할 권한이 없습니다\"}"
                        )
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "채팅방 또는 사용자를 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "CHAT_ROOM_NOT_FOUND",
                                        value = "{\"errorCodeName\":\"CHAT_ROOM_NOT_FOUND\",\"errorMessage\":\"채팅방을 찾을 수 없습니다\"}"
                                ),
                                @ExampleObject(
                                        name = "CHAT_MEMBER_NOT_FOUND",
                                        value = "{\"errorCodeName\":\"CHAT_MEMBER_NOT_FOUND\",\"errorMessage\":\"채팅 사용자를 찾을 수 없습니다\"}"
                                )
                        }
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "INTERNAL_SERVER_ERROR",
                                value = "{\"errorCodeName\":\"INTERNAL_SERVER_ERROR\",\"errorMessage\":\"서버 내부 오류입니다\"}"
                        )
                )
            )
    })
    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<ChatReadResponse> markAllMessagesAsReadInRoom(
            @Parameter(description = "읽음 처리할 채팅방 ID", required = true)
            @PathVariable Long roomId,
            @RequestBody ChatRoomReadRequest request) {

        Long authenticatedMemberId = memberUtil.getCurrentUser().memberId();
        log.info("채팅방 전체 메시지 읽음 처리 요청: roomId={}, authenticatedMemberId={}", roomId, authenticatedMemberId);

        try {
            int readCount = chatMessageService.markAllMessagesAsReadInRoom(roomId, authenticatedMemberId);
            ChatReadResponse readResponse = ChatReadResponse.ofRoom(roomId, authenticatedMemberId, readCount, true);

            log.info("채팅방 전체 메시지 읽음 처리 완료: roomId={}, readCount={}", roomId, readCount);
            return ResponseEntity.ok(readResponse);

        } catch (Exception e) {
            log.error("채팅방 전체 메시지 읽음 처리 실패: roomId={}, error={}", roomId, e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리하도록 예외를 다시 throw
        }
    }

    /**
     * 현재 사용자의 전체 읽지 않은 메시지 개수를 조회하는 API
     * @return 읽지 않은 메시지 개수
     */
    @Operation(summary = "전체 읽지 않은 메시지 개수 조회 API", description = "사용자의 전체 읽지 않은 메시지 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatUnreadCountResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (요청 값 검증 실패)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "INVALID_ARGUMENT",
                                value = "{\"errorCodeName\":\"INVALID_ARGUMENT\",\"errorMessage\":\"유효하지 않은 인자입니다\"}"
                        )
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "CHAT_MEMBER_NOT_FOUND",
                                value = "{\"errorCodeName\":\"CHAT_MEMBER_NOT_FOUND\",\"errorMessage\":\"채팅 사용자를 찾을 수 없습니다\"}"
                        )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "INTERNAL_SERVER_ERROR",
                                value = "{\"errorCodeName\":\"INTERNAL_SERVER_ERROR\",\"errorMessage\":\"서버 내부 오류입니다\"}"
                        )
                )
            )
    })
    @GetMapping("/messages/unread-count")
    public ResponseEntity<ChatUnreadCountResponse> getUnreadMessageCount() {
        Long authenticatedMemberId = memberUtil.getCurrentUser().memberId();
        log.info("읽지 않은 메시지 개수 조회 요청: authenticatedMemberId={}", authenticatedMemberId);

        try {
            long unreadCount = chatMessageService.getUnreadMessageCount(authenticatedMemberId);
            ChatUnreadCountResponse response = ChatUnreadCountResponse.ofTotal(authenticatedMemberId, unreadCount);

            log.info("읽지 않은 메시지 개수 조회 완료: authenticatedMemberId={}, count={}", authenticatedMemberId, unreadCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("읽지 않은 메시지 개수 조회 실패: authenticatedMemberId={}, error={}", authenticatedMemberId, e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리하도록 예외를 다시 throw
        }
    }

    /**
     * 현재 사용자가 특정 채팅방에서 읽지 않은 메시지 개수를 조회하는 API
     * @param roomId 채팅방 ID
     * @return 읽지 않은 메시지 개수
     */
    @Operation(summary = "채팅방 읽지 않은 메시지 개수 조회 API", description = "특정 채팅방의 읽지 않은 메시지 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatUnreadCountResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (요청 값 검증 실패)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "INVALID_ARGUMENT",
                                value = "{\"errorCodeName\":\"INVALID_ARGUMENT\",\"errorMessage\":\"유효하지 않은 인자입니다\"}"
                        )
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description = "채팅방 접근 권한 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "CHAT_PARTICIPANT_NOT_FOUND",
                                value = "{\"errorCodeName\":\"CHAT_PARTICIPANT_NOT_FOUND\",\"errorMessage\":\"채팅방에 참여할 권한이 없습니다\"}"
                        )
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "채팅방 또는 사용자를 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "CHAT_ROOM_NOT_FOUND",
                                        value = "{\"errorCodeName\":\"CHAT_ROOM_NOT_FOUND\",\"errorMessage\":\"채팅방을 찾을 수 없습니다\"}"
                                ),
                                @ExampleObject(
                                        name = "CHAT_MEMBER_NOT_FOUND",
                                        value = "{\"errorCodeName\":\"CHAT_MEMBER_NOT_FOUND\",\"errorMessage\":\"채팅 사용자를 찾을 수 없습니다\"}"
                                )
                        }
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "INTERNAL_SERVER_ERROR",
                                value = "{\"errorCodeName\":\"INTERNAL_SERVER_ERROR\",\"errorMessage\":\"서버 내부 오류입니다\"}"
                        )
                )
            )
    })
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<ChatUnreadCountResponse> getUnreadMessageCountInRoom(
            @Parameter(description = "읽지 않은 메시지 개수를 조회할 채팅방 ID", required = true)
            @PathVariable Long roomId) {

        Long authenticatedMemberId = memberUtil.getCurrentUser().memberId();
        log.info("채팅방 읽지 않은 메시지 개수 조회 요청: roomId={}, authenticatedMemberId={}", roomId, authenticatedMemberId);

        try {
            long unreadCount = chatMessageService.getUnreadMessageCountInRoom(roomId, authenticatedMemberId);
            ChatUnreadCountResponse response = ChatUnreadCountResponse.ofRoom(authenticatedMemberId, roomId, unreadCount);

            log.info("채팅방 읽지 않은 메시지 개수 조회 완료: roomId={}, authenticatedMemberId={}, count={}", roomId, authenticatedMemberId, unreadCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("채팅방 읽지 않은 메시지 개수 조회 실패: roomId={}, authenticatedMemberId={}, error={}", roomId, authenticatedMemberId, e.getMessage());
            throw e; // GlobalExceptionHandler에서 처리하도록 예외를 다시 throw
        }
    }
}
