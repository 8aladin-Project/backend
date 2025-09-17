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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // product 엔티티 구현시 주석 해제
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "product_id", nullable = false)
    // private Long productId;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();


    // ChatRoom 생성자
    public static ChatRoom create(Member member) {
        return ChatRoom.builder()
                .member(member)
                // .productId(productId)
                .build();
    }

    // 메시지 추가 메서드
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
    }

}
