package potato.backend.domain.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import potato.backend.domain.common.domain.BaseEntity;
import potato.backend.domain.user.domain.Member;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_member_id")
    private Member createdBy; // 채팅방 생성자

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoomParticipant> participants = new ArrayList<>();

    // product 엔티티 구현시 주석 해제
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "product_id", nullable = false)
    // private Long productId;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();


    // ChatRoom 생성자
    public static ChatRoom create(Member createdBy) {
        return ChatRoom.builder()
                .createdBy(createdBy)
                .build();
    }

    // 채팅방 참여자 추가
    public void addParticipant(ChatRoomParticipant participant) {
        if (!this.participants.contains(participant)) {
            this.participants.add(participant);
            participant.assignChatRoom(this);
        }
    }

    // 메시지 추가 메서드
    public void addMessage(ChatMessage message) {
        if (message.getSender() == null || !this.equals(message.getSender().getChatRoom())) {
            throw new IllegalArgumentException("Message sender must be a participant of the chat room");
        }
        this.messages.add(message);
        message.assignChatRoom(this);
    }

}
