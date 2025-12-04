package potato.backend.domain.user.dto;

import lombok.Getter;

/**
 * 회원가입 응답 DTO
 */
@Getter
public class SignUpResponse {

    private Long memberId;
    private String name;
    private String email;
    private String message;

    private SignUpResponse(Long memberId, String name, String email, String message) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.message = message;
    }

    public static SignUpResponse of(Long memberId, String name, String email) {
        return new SignUpResponse(memberId, name, email, "회원가입이 성공적으로 완료되었습니다");
    }
}

