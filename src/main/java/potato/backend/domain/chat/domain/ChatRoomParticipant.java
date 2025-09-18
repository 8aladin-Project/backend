package potato.backend.domain.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import potato.backend.domain.common.domain.BaseEntity;
import potato.backend.domain.user.domain.Member;

import java.time.Instant;

@Entity
@Table(name = "chat_room_participants")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id")
    private ChatMessage lastReadMessage;

    // ChatRoomParticipant 생성자 메서드
    public static ChatRoomParticipant create(ChatRoom chatRoom, Member member) {
        ChatRoomParticipant participant = ChatRoomParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .joinedAt(Instant.now())
                .build();
        chatRoom.addParticipant(participant);
        return participant;
    }

    // Persist 시점에 대한 처리
    @PrePersist
    protected void onPersist() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
    }

    // 양방향 연관관계 설정 메서드 - 채팅방을 설정
    public void assignChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        if (!chatRoom.getParticipants().contains(this)) {
            chatRoom.getParticipants().add(this);
        }
    }

    // 양방향 연관관계 설정 메서드 - 멤버를 설정
    public void assignMember(Member member) {
        this.member = member;
    }

    // 마지막으로 읽은 메시지 업데이트 메서드
    public void updateLastReadMessage(ChatMessage chatMessage) {
        this.lastReadMessage = chatMessage;
    }
}
