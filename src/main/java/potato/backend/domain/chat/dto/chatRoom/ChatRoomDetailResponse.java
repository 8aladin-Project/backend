package potato.backend.domain.chat.dto.chatRoom;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 상세 조회 응답 DTO
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomDetailResponse {

    private boolean success;
    private Data data;

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Data {
        private String id;
        private List<Participant> participants;
        private ProductInfo product;
        private String createdAt;
        private String updatedAt;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Participant {
        private String userId;
        private String userName;
        private String profileImage; // 현재는 null (프로필 이미지 기능 없음)
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProductInfo {
        private String id;
        private String name;
        private long price;
        private String image;
        private String status;
    }

    public static ChatRoomDetailResponse success(
            String roomId,
            List<Participant> participants,
            ProductInfo product,
            String createdAt,
            String updatedAt) {

        Data data = Data.builder()
                .id(roomId)
                .participants(participants)
                .product(product)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        return ChatRoomDetailResponse.builder()
                .success(true)
                .data(data)
                .build();
    }

    public static Participant ofParticipant(potato.backend.domain.user.domain.Member member) {
        return Participant.builder()
                .userId(member.getId().toString())
                .userName(member.getName())
                .profileImage(null) // 프로필 이미지 기능이 아직 없음
                .build();
    }

    public static ProductInfo ofProduct(potato.backend.domain.product.domain.Product product) {
        String statusString;
        if (product.getStatus() == potato.backend.domain.product.domain.Status.SELLING) {
            statusString = "판매중";
        } else {
            statusString = "판매완료";
        }

        return ProductInfo.builder()
                .id(product.getId().toString())
                .name(product.getTitle())
                .price(product.getPrice().longValue())
                .image(product.getMainImageUrl())
                .status(statusString)
                .build();
    }
}
