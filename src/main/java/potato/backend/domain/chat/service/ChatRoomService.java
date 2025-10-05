package potato.backend.domain.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import potato.backend.domain.chat.domain.ChatRoom;
import potato.backend.domain.chat.dto.ChatRoomCreateRequest;
import potato.backend.domain.chat.dto.ChatRoomResponse;
import potato.backend.domain.chat.repository.ChatRoomRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request) {
        validateDistinctParticipants(request);

        Member seller = getMember(request.getSellerId());
        Member buyer = getMember(request.getBuyerId());

        ChatRoom chatRoom = chatRoomRepository.findByParticipants(seller, buyer)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.create(seller, buyer)));

        return ChatRoomResponse.from(chatRoom);
    }

    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found: " + chatRoomId));
        return ChatRoomResponse.from(chatRoom);
    }

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found: " + memberId));
    }

    private void validateDistinctParticipants(ChatRoomCreateRequest request) {
        if (request.getSellerId().equals(request.getBuyerId())) {
            throw new IllegalArgumentException("Seller and buyer must be different members");
        }
    }
}
