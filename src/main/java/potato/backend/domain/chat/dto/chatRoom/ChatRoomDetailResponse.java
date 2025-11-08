package potato.backend.domain.chat.dto.chatRoom;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 상세 조회 응답 DTO
 */
@Getter
@Schema(description = "채팅방 상세 조회 응답", name = "ChatRoomDetailResponse")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomDetailResponse {

    @Schema(description = "성공 여부")
    private boolean success;

    @Schema(description = "응답 데이터")
    private Data data;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Schema(description = "응답 데이터")
    public static class Data {
        @Schema(description = "채팅방 ID")
        private String id;

        @Schema(description = "참가자 목록")
        private List<Participant> participants;

        @Schema(description = "상품 정보")
        private ProductInfo product;

        @Schema(description = "생성 시각 (ISO 형식)")
        private String createdAt;

        @Schema(description = "갱신 시각 (ISO 형식)")
        private String updatedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Schema(description = "참가자 정보")
    public static class Participant {
        @Schema(description = "사용자 ID")
        private String userId;

        @Schema(description = "사용자 이름")
        private String userName;

        @Schema(description = "프로필 이미지 URL", nullable = true)
        private String profileImage; // 현재는 null (프로필 이미지 기능 없음)
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Schema(description = "상품 정보")
    public static class ProductInfo {
        @Schema(description = "상품 ID")
        private String id;

        @Schema(description = "상품명")
        private String name;

        @Schema(description = "가격 (원)")
        private long price;

        @Schema(description = "상품 이미지 URL")
        private String image;

        @Schema(description = "판매 상태", example = "SELLING")
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
        switch (product.getStatus()) {
            case SELLING:
                statusString = product.getStatus().name(); // "SELLING"
                break;
            case SOLD_OUT:
                statusString = product.getStatus().name(); // "SOLD_OUT"
                break;
            default:
                throw new IllegalArgumentException("Unknown product status: " + product.getStatus());
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
