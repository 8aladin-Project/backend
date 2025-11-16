package potato.backend.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * FCM 토큰 등록 요청 DTO
 */
@Getter
public class FcmTokenRequest {

    @NotBlank(message = "FCM 토큰은 필수입니다")
    private String fcmToken;
}

