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
@RequestMapping("/api/chat-rooms")
@CrossOrigin(origins = "*")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(@Valid @RequestBody ChatRoomCreateRequest request) {
        ChatRoomResponse response = chatRoomService.createChatRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{chatRoomId}")
    public ChatRoomResponse getChatRoom(@PathVariable Long chatRoomId) {
        return chatRoomService.getChatRoom(chatRoomId);
    }

    @GetMapping
    public List<ChatRoomResponse> getChatRooms(@RequestParam(name = "memberId", required = false) Long memberId) {
        return chatRoomService.getChatRooms(memberId);
    }
}
