package potato.backend.global.security.jwt;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import potato.backend.global.security.oauth.UserInfo;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public String createAccessToken(UserInfo userInfo) {
        return jwtUtil.generateAccessToken(userInfo);
    }

    public String createRefreshToken(UserInfo userInfo) {
        String refreshToken = jwtUtil.generateRefreshToken(userInfo);
        refreshTokenRepository.save(RefreshToken.create(userInfo.memberId(), refreshToken));
        return refreshToken;
    }
}
