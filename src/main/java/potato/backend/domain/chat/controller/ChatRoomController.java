package potato.backend.domain.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import potato.backend.domain.chat.dto.chatMessage.ChatRoomCreateRequest;
import potato.backend.domain.chat.dto.chatRoom.ChatRoomDetailResponse;
import potato.backend.domain.chat.dto.chatRoom.ChatRoomListResponse;
import potato.backend.domain.chat.dto.chatRoom.ChatRoomResponse;
import potato.backend.domain.chat.service.ChatRoomService;
import potato.backend.global.exception.ErrorResponse;

/**
 * 채팅방 컨트롤러
 */
@RestController 
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatrooms")
@Tag(name = "ChatRooms", description = "채팅방 생성 및 조회 API")
@CrossOrigin(origins = "*") // 모든 경로에서 접근 허용, 프론트엔드 포트에 맞게 설정 필요
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 채팅방 생성 메서드
     * @param request 채팅방 생성 요청 DTO
     * @return 채팅방 생성 결과
     */
    @Operation(summary = "채팅방 생성 API", description = "판매자, 구매자, 상품의 아이디를 기준으로 새로운 채팅방을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "채팅방 생성 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "INVALID_CHAT_ROOM_PARTICIPANTS",
                                    value = "{\"errorCodeName\":\"INVALID_CHAT_ROOM_PARTICIPANTS\",\"errorMessage\":\"채팅방 참가자가 유효하지 않습니다\"}"
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
    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(@Valid @RequestBody ChatRoomCreateRequest request) {
        ChatRoomResponse response = chatRoomService.createChatRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 채팅방 상세 조회 메서드
     * @param chatRoomId 채팅방 아이디
     * @return 채팅방 상세 정보
     */
    @Operation(summary = "채팅방 상세 조회 API", description = "채팅방 ID로 채팅방의 상세 정보(참가자, 상품 정보)를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatRoomDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "CHAT_ROOM_NOT_FOUND",
                                    value = "{\"errorCodeName\":\"CHAT_ROOM_NOT_FOUND\",\"errorMessage\":\"채팅방을 찾을 수 없습니다\"}"
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
    @GetMapping("/{chatRoomId}")
    public ChatRoomDetailResponse getChatRoom(@PathVariable Long chatRoomId) {
        return chatRoomService.getChatRoom(chatRoomId);
    }

    /**
     * memberId를 기준으로 채팅방 목록 조회 메서드
     * @param memberId 회원 아이디
     * @return 채팅방 목록
     */
    @Operation(summary = "채팅방 목록 조회 API", description = "memberId를 기준으로 해당 회원이 참여 중인 채팅방을 상세 정보와 함께 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatRoomListResponse.class))
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
    @GetMapping
    public ChatRoomListResponse getChatRooms(
            @Parameter(description = "회원 ID, 참여 중인 채팅방만 반환합니다.", required = true)
            @RequestParam(name = "memberId") Long memberId) {
        return chatRoomService.getChatRoomList(memberId);
    }
}
