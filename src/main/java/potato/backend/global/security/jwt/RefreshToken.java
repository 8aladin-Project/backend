package potato.backend.global.security.jwt;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "refresh_token", timeToLive = 1209600) // 14 days to match JWT expiration
public class RefreshToken {

    @Id
    private Long memberId;

    private String token;

    public static RefreshToken create(Long memberId, String token) {
        return RefreshToken.builder().memberId(memberId).token(token).build();
    }
}
