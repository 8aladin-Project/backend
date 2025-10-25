package potato.backend.domain.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import potato.backend.domain.chat.exception.ChatMessageInvalidException;
import potato.backend.domain.chat.exception.ChatParticipantNotFoundException;
import potato.backend.domain.common.domain.BaseEntity;
import potato.backend.domain.user.domain.Member;

import java.time.Instant;

// 채팅방 메시지 엔티티
@Entity
@Table(name = "chat_messages")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false; // 읽음여부는 기본적으로 false

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    // ChatMessage 생성자 메서드
    public static ChatMessage create(Member sender, ChatRoom chatRoom, String content) {
        validateParticipant(sender, chatRoom);
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .isRead(false)
                .sentAt(Instant.now())
                .build();
    }

    // ChatMessage를 읽었을때 1로 처리 
    public void markAsRead() {
        this.isRead = true;
    }

    // 양방향 연관관계 설정 메서드 - 채팅방을 설정
    public void assignChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    // 양방향 연관관계 설정 메서드 - 메시지 전송자를 설정
    public void assignSender(Member sender) {
        this.sender = sender;
    }

    // chatMessage 엔티티가 DB에 저장되기 직전에 자동으로 실행되는 메서드
    @PrePersist // 엔티티가 처음 저장(persist, INSERT)되는 상황에서 실행됨
    protected void onPersist() {
        if (this.sentAt == null) {
            this.sentAt = Instant.now();
        }
        validateParticipant(this.sender, this.chatRoom);
    }

    // 유효성 검사 메서드
    private static void validateParticipant(Member participant, ChatRoom chatRoom) {
        if (participant == null) {
            throw new ChatMessageInvalidException("메시지 발신자는 null일 수 없습니다");
        }
        if (chatRoom == null) {
            throw new ChatMessageInvalidException("메시지 채팅방은 null일 수 없습니다");
        }
        if (!chatRoom.isParticipant(participant)) {
            throw new ChatParticipantNotFoundException("발신자가 채팅방에 참여하지 않았습니다");
        }
    }
}
