package potato.backend.domain.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import potato.backend.domain.chat.domain.ChatRoom;
import potato.backend.domain.chat.dto.chatMessage.ChatRoomCreateRequest;
import potato.backend.domain.chat.dto.chatRoom.ChatRoomResponse;
import potato.backend.domain.chat.exception.ChatRoomNotFoundException;
import potato.backend.domain.chat.exception.InvalidChatRoomParticipantsException;
import potato.backend.domain.chat.exception.MemberNotFoundException;
import potato.backend.domain.chat.repository.ChatRoomRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    /**
     * 채팅방 생성 메서드
     * @param request 채팅방 생성 요청 DTO
     * @return 채팅방 생성 결과
     */
    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request) {
        validateDistinctParticipants(request);

        Member seller = getMember(request.getSellerId());
        Member buyer = getMember(request.getBuyerId());

        ChatRoom chatRoom = chatRoomRepository.findByParticipants(seller, buyer)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.create(seller, buyer)));

        return ChatRoomResponse.from(chatRoom);
    }


    /**
     * 채팅방 단건 조회 메서드
     * @param chatRoomId
     * @return 
     */
    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
        return ChatRoomResponse.from(chatRoom);
    }

    /**
     * memberId를 기준으로 채팅방 목록 조회 메서드
     * @param memberId
     * @return
     */
    public List<ChatRoomResponse> getChatRooms(Long memberId) {
        List<ChatRoom> chatRooms = (memberId != null)
                ? chatRoomRepository.findAllByMemberId(memberId)
                : chatRoomRepository.findAll();

        return chatRooms.stream()
                .map(ChatRoomResponse::from)
                .toList();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private void validateDistinctParticipants(ChatRoomCreateRequest request) {
        if (request.getSellerId().equals(request.getBuyerId())) {
            throw new InvalidChatRoomParticipantsException();
        }
    }
}
