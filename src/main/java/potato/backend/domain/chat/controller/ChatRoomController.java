package potato.backend.domain.chat.controller;

import java.util.List;

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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import potato.backend.domain.chat.dto.ChatRoomCreateRequest;
import potato.backend.domain.chat.dto.ChatRoomResponse;
import potato.backend.domain.chat.service.ChatRoomService;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat-rooms")
@CrossOrigin(origins = "*") // 모든 경로에서 접근 허용 
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 채팅방 생성 메서드
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(@Valid @RequestBody ChatRoomCreateRequest request) {
        ChatRoomResponse response = chatRoomService.createChatRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 채팅방 단건 조회 메서드
     * @param chatRoomId
     * @return
     */
    @GetMapping("/{chatRoomId}")
    public ChatRoomResponse getChatRoom(@PathVariable Long chatRoomId) {
        return chatRoomService.getChatRoom(chatRoomId);
    }

    /**
     * memberId를 기준으로 채팅방 목록 조회 메서드
     * @param memberId
     * @return
     */
    @GetMapping
    public List<ChatRoomResponse> getChatRooms(@RequestParam(name = "memberId", required = false) Long memberId) {
        return chatRoomService.getChatRooms(memberId);
    }
}
