package potato.backend.domain.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import potato.backend.domain.common.domain.BaseEntity;
import potato.backend.domain.user.domain.Member;

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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false; // 읽음여부는 기본적으로 false

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    // ChatMessage 생성자 메서드
    public static ChatMessage create(Member member, ChatRoom chatRoom, String content) {
        return ChatMessage.builder()
                .member(member)
                .chatRoom(chatRoom)
                .content(content)
                .isRead(false)
                .sentAt(Instant.now())
                .build();
    }

    // ChatMessage를 읽었을때 1로 처리 
    public void markAsRead() {
        this.isRead = true;
    }
}
