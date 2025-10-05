package potato.backend.domain.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import potato.backend.domain.chat.dto.ChatMessageResponse;
import potato.backend.domain.chat.dto.ChatSendRequest;
import potato.backend.domain.chat.service.ChatMessageService;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private static final String TOPIC_PREFIX = "/topic/chat/"; // 클라이언트가 구독하는 채널 주소 앞 부분

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate; // 스프링에 제공하는 메시지 전송 도구

    @MessageMapping("/chat/{roomId}")
    public void handleMessage(@DestinationVariable Long roomId, Map<String, Object> payload) {
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        String content = (String) payload.get("content");

        System.out.println("메시지 수신: roomId=" + roomId + ", senderId=" + senderId + ", content=" + content);

        ChatSendRequest request = ChatSendRequest.of(senderId, content);
        ChatMessageResponse response = chatMessageService.sendMessage(roomId, request);
        // 특정 경로로 메시지를 보내는 메서드
        // /topic/chat/{roomId} 경로로 response를 전송
        messagingTemplate.convertAndSend(TOPIC_PREFIX + roomId, response);
    }
}
