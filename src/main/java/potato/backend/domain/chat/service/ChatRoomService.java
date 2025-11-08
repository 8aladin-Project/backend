package potato.backend.domain.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import potato.backend.domain.chat.domain.ChatRoom;
import potato.backend.domain.chat.dto.chatMessage.ChatRoomCreateRequest;
import potato.backend.domain.chat.dto.chatRoom.ChatRoomDetailResponse;
import potato.backend.domain.chat.dto.chatRoom.ChatRoomListResponse;
import potato.backend.domain.chat.dto.chatRoom.ChatRoomResponse;
import potato.backend.domain.chat.exception.ChatRoomNotFoundException;
import potato.backend.domain.chat.exception.InvalidChatRoomParticipantsException;
import potato.backend.domain.chat.exception.MemberNotFoundException;
import potato.backend.domain.chat.repository.ChatMessageRepository;
import potato.backend.domain.chat.repository.ChatRoomRepository;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.product.repository.ProductRepository;
import potato.backend.domain.user.domain.Member;
import potato.backend.domain.user.repository.MemberRepository;

import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

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
        Product product = getProduct(request.getProductId());

        ChatRoom chatRoom = chatRoomRepository.findByParticipantsAndProduct(seller, buyer, product)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.create(seller, buyer, product)));

        return ChatRoomResponse.from(chatRoom);
    }


    /**
     * 채팅방 단건 조회 메서드
     * @param chatRoomId 채팅방 ID
     * @return 채팅방 상세 정보
     */
    public ChatRoomDetailResponse getChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

        // 참가자 정보 생성
        List<ChatRoomDetailResponse.Participant> participants = List.of(
                ChatRoomDetailResponse.ofParticipant(chatRoom.getSeller()),
                ChatRoomDetailResponse.ofParticipant(chatRoom.getBuyer())
        );

        // 상품 정보 생성
        ChatRoomDetailResponse.ProductInfo product = null;
        if (chatRoom.getProduct() != null) {
            product = ChatRoomDetailResponse.ofProduct(chatRoom.getProduct());
        }

        return ChatRoomDetailResponse.success(
                chatRoom.getId().toString(),
                participants,
                product,
                chatRoom.getCreatedAt().toString(),
                chatRoom.getUpdatedAt().toString()
        );
    }

    /**
     * memberId를 기준으로 채팅방 목록 조회 메서드 (기존 API 호환용)
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

    /**
     * 사용자의 채팅방 목록을 상세 정보와 함께 조회
     * @param memberId 사용자 ID
     * @return 채팅방 목록 응답
     */
    public ChatRoomListResponse getChatRoomList(Long memberId) {
        // 사용자의 채팅방 목록 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByMemberId(memberId);

        // 각 채팅방의 상세 정보 계산
        List<ChatRoomListResponse.ChatRoomSummary> roomSummaries = chatRooms.stream()
                .map(chatRoom -> createChatRoomSummary(chatRoom, memberId))
                .sorted(Comparator.comparing(ChatRoomListResponse.ChatRoomSummary::getTimestamp,
                                             Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        return ChatRoomListResponse.success(roomSummaries);
    }

    /**
     * 채팅방 요약 정보 생성
     */
    private ChatRoomListResponse.ChatRoomSummary createChatRoomSummary(ChatRoom chatRoom, Long currentUserId) {
        // 상대방 정보 결정
        Member otherParticipant = getOtherParticipant(chatRoom, currentUserId);

        // 마지막 메시지 조회
        String lastMessage = getLastMessageContent(chatRoom);
        String lastMessageTime = getLastMessageTime(chatRoom);

        // 읽지 않은 메시지 개수 계산
        long unreadCount = chatMessageRepository.countByChatRoomAndIsReadAndSenderNot(
                chatRoom, false, getCurrentUser(currentUserId));

        // 상품 정보
        String productImage = chatRoom.getProduct() != null ? chatRoom.getProduct().getMainImageUrl() : null;
        String productName = chatRoom.getProduct() != null ? chatRoom.getProduct().getTitle() : null;
        Long productPrice = chatRoom.getProduct() != null ? chatRoom.getProduct().getPrice().longValue() : 0L;

        return ChatRoomListResponse.ofRoom(
                chatRoom.getId(),
                otherParticipant.getName(),
                otherParticipant.getId().toString(),
                productImage,
                productName,
                chatRoom.getProduct() != null ? chatRoom.getProduct().getId().toString() : null,
                productPrice,
                lastMessage,
                lastMessageTime,
                unreadCount,
                false // 온라인 상태 (현재 구현되지 않음)
        );
    }

    /**
     * 채팅방에서 현재 사용자를 제외한 상대방 반환
     */
    private Member getOtherParticipant(ChatRoom chatRoom, Long currentUserId) {
        if (chatRoom.getSeller().getId().equals(currentUserId)) {
            return chatRoom.getBuyer();
        } else {
            return chatRoom.getSeller();
        }
    }

    /**
     * 채팅방의 마지막 메시지 내용 조회
     */
    private String getLastMessageContent(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoomOrderBySentAtAsc(chatRoom)
                .stream()
                .max(Comparator.comparing(potato.backend.domain.chat.domain.ChatMessage::getSentAt))
                .map(potato.backend.domain.chat.domain.ChatMessage::getContent)
                .orElse(null);
    }

    /**
     * 채팅방의 마지막 메시지 시간 조회
     */
    private String getLastMessageTime(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoomOrderBySentAtAsc(chatRoom)
                .stream()
                .max(Comparator.comparing(potato.backend.domain.chat.domain.ChatMessage::getSentAt))
                .map(msg -> msg.getSentAt().toString())
                .orElse(null);
    }

    /**
     * 현재 사용자 엔티티 조회
     */
    private Member getCurrentUser(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }

    private void validateDistinctParticipants(ChatRoomCreateRequest request) {
        if (request.getSellerId().equals(request.getBuyerId())) {
            throw new InvalidChatRoomParticipantsException();
        }
    }
}
