package potato.backend.domain.user.dto;

import lombok.Getter;

/**
 * FCM 토큰 등록 응답 DTO
 */
@Getter
public class FcmTokenResponse {

    private Long memberId;
    private String fcmToken;
    private String message;

    private FcmTokenResponse(Long memberId, String fcmToken, String message) {
        this.memberId = memberId;
        this.fcmToken = fcmToken;
        this.message = message;
    }

    public static FcmTokenResponse of(Long memberId, String fcmToken) {
        return new FcmTokenResponse(memberId, fcmToken, "FCM 토큰이 성공적으로 등록되었습니다");
    }
}

