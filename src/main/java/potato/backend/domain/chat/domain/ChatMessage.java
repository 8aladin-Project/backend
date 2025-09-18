package potato.backend.domain.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import potato.backend.domain.common.domain.BaseEntity;

import java.time.Instant;

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
    @JoinColumn(name = "chat_room_participant_id", nullable = false)
    private ChatRoomParticipant sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false; // 읽음여부는 기본적으로 false

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    // ChatMessage 생성자 메서드
    public static ChatMessage create(ChatRoomParticipant sender, ChatRoom chatRoom, String content) {
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
    public void assignSender(ChatRoomParticipant sender) {
        this.sender = sender;
    }

    // Persist 시점에 대한 처리
    @PrePersist
    protected void onPersist() {
        if (this.sentAt == null) {
            this.sentAt = Instant.now();
        }
        if (this.chatRoom == null && this.sender != null) {
            this.chatRoom = this.sender.getChatRoom();
        }
        validateParticipant(this.sender, this.chatRoom);
    }

    // 유효성 검사 메서드
    private static void validateParticipant(ChatRoomParticipant participant, ChatRoom chatRoom) {
        if (participant == null) {
            throw new IllegalStateException("Chat message sender must not be null");
        }
        if (chatRoom == null) {
            throw new IllegalStateException("Chat message chat room must not be null");
        }
        if (participant.getChatRoom() == null || !participant.getChatRoom().equals(chatRoom)) {
            throw new IllegalArgumentException("Sender is not a participant of the chat room");
        }
    }
}
