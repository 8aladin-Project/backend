package potato.backend.domain.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import potato.backend.domain.common.domain.BaseEntity;
import potato.backend.domain.product.domain.Product;
import potato.backend.domain.user.domain.Member;

import java.util.ArrayList;
import java.util.List;

// 채팅방 엔티티
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
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller; // 상품을 등록한 판매자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer; // 구매를 시도하는 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product productId;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();


    // ChatRoom 생성자
    public static ChatRoom create(Member seller, Member buyer) {
        return ChatRoom.builder()
                .seller(seller)
                .buyer(buyer)
                .build();
    }

    // 메시지 추가 메서드
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        message.assignChatRoom(this);
    }

    // 채팅방 참여자 확인 메서드
    public boolean isParticipant(Member member) {
        return member != null && (member.equals(seller) || member.equals(buyer));
    }

}
