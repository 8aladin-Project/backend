package potato.backend.domain.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import potato.backend.domain.chat.domain.ChatMessage;
import potato.backend.domain.chat.domain.ChatRoom;
import potato.backend.domain.chat.dto.ChatMessageResponse;
import potato.backend.domain.chat.dto.ChatSendRequest;
import potato.backend.domain.chat.repository.ChatMessageRepository;
import potato.backend.domain.chat.repository.ChatRoomRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    /**
     * 메시지 전송 메서드
     * 채팅방과 발신자를 확인한 뒤 메시지를 생성 및 저장
     * 이후 메시지 전송 DTO로 변환하여 반환하는 메서드
     * @param roomId
     * @param request
     * @return
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long roomId, ChatSendRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Chat room not found: " + roomId));

        Member sender = memberRepository.findById(request.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + request.getSenderId()));

        if (!chatRoom.isParticipant(sender)) {
            throw new IllegalArgumentException("Sender is not a participant of the chat room");
        }

        ChatMessage message = ChatMessage.create(sender, chatRoom, request.getContent());
        chatRoom.addMessage(message);

        ChatMessage savedMessage = chatMessageRepository.save(message);
        return ChatMessageResponse.from(savedMessage);
    }
}
