package potato.backend.global.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private Long memberId;
    private String name;
    private String email;
}

