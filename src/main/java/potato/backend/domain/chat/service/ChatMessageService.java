package potato.backend.domain.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import potato.backend.domain.chat.domain.ChatMessage;
import potato.backend.domain.chat.domain.ChatRoom;
import potato.backend.domain.chat.dto.chatMessage.ChatMessageResponse;
import potato.backend.domain.chat.dto.chatMessage.ChatSendRequest;
import potato.backend.domain.chat.exception.ChatMessageNotFoundException;
import potato.backend.domain.chat.exception.ChatParticipantNotFoundException;
import potato.backend.domain.chat.exception.ChatRoomNotFoundException;
import potato.backend.domain.chat.exception.MemberNotFoundException;
import potato.backend.domain.chat.repository.ChatMessageRepository;
import potato.backend.domain.chat.repository.ChatRoomRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;


import java.util.List;

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
     * @param roomId 채팅방 아이디
     * @param request 메시지 전송 요청 DTO
     * @return 메시지 전송 결과
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long roomId, ChatSendRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        Member sender = memberRepository.findById(request.getSenderId())
                .orElseThrow(() -> new MemberNotFoundException(request.getSenderId()));

        if (!chatRoom.isParticipant(sender)) {
            throw new ChatParticipantNotFoundException(sender.getId(), roomId);
        }

        ChatMessage message = ChatMessage.create(sender, chatRoom, request.getContent());
        chatRoom.addMessage(message);

        ChatMessage savedMessage = chatMessageRepository.save(message);
        return ChatMessageResponse.from(savedMessage);
    }

    /**
     * 특정 메시지를 읽음 처리하는 메서드
     * @param messageId 메시지 ID
     * @param memberId 읽은 사용자 ID
     * @return 읽음 처리된 메시지 응답
     */
    @Transactional
    public ChatMessageResponse markMessageAsRead(Long messageId, Long memberId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatMessageNotFoundException(messageId));

        // 메시지를 읽지 않은 경우에만 읽음 처리
        if (!message.isRead()) {
            message.markAsRead();
            chatMessageRepository.save(message);
        }

        return ChatMessageResponse.from(message);
    }

    /**
     * 채팅방의 모든 메시지를 읽음 처리하는 메서드 (특정 사용자가 보낸 메시지를 제외)
     * @param roomId 채팅방 ID
     * @param memberId 읽은 사용자 ID
     * @return 읽음 처리된 메시지 개수
     */
    @Transactional
    public int markAllMessagesAsReadInRoom(Long roomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!chatRoom.isParticipant(member)) {
            throw new ChatParticipantNotFoundException(member.getId(), roomId);
        }

        // 해당 사용자가 보낸 메시지를 제외하고 읽지 않은 메시지들을 읽음 처리
        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatRoomAndIsReadAndSenderNot(chatRoom, false, member);

        for (ChatMessage message : unreadMessages) {
            message.markAsRead();
        }

        chatMessageRepository.saveAll(unreadMessages);
        return unreadMessages.size();
    }

    /**
     * 특정 사용자가 읽지 않은 메시지 개수를 조회하는 메서드
     * @param memberId 사용자 ID
     * @return 읽지 않은 메시지 개수
     */
    public long getUnreadMessageCount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        return chatMessageRepository.countByIsReadAndSenderNot(false, member);
    }

    /**
     * 채팅방에서 특정 사용자가 읽지 않은 메시지 개수를 조회하는 메서드
     * @param roomId 채팅방 ID
     * @param memberId 사용자 ID
     * @return 읽지 않은 메시지 개수
     */
    public long getUnreadMessageCountInRoom(Long roomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        return chatMessageRepository.countByChatRoomAndIsReadAndSenderNot(chatRoom, false, member);
    }
}
